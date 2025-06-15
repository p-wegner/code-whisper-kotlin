package dev.aider.core

import dev.aider.cli.AiderCommand
import dev.aider.openai.OpenAIClient
import dev.aider.anthropic.AnthropicClient
import dev.aider.openrouter.OpenRouterClient
import dev.aider.deepseek.DeepSeekClient
import dev.aider.file.FileManager
import dev.aider.output.OutputFormatter
import dev.aider.edit.EditParser
import dev.aider.edit.EditApplier
import dev.aider.edit.EditStatus
import dev.aider.git.GitManager
import dev.aider.retry.RetryManager
import dev.aider.repomap.RepoMapGenerator
import dev.aider.history.ChatHistory
import dev.aider.history.InputHistory
import dev.aider.vertexai.VertexAIClient

class AiderCore {
    private val fileManager = FileManager()
    private val outputFormatter = OutputFormatter()
    private val editParser = EditParser()
    
    suspend fun execute(command: AiderCommand) {
        try {
            outputFormatter.printHeader()
            
            // Initialize chat history and input history
            val chatHistory = ChatHistory(verbose = command.verbose)
            val inputHistory = InputHistory(verbose = command.verbose)
            
            chatHistory.initializeHistory()
            
            // Add current input to history
            inputHistory.addInput(command.message, command.model, command.files)
            
            if (command.verbose) {
                println("Using model: ${command.model}")
                println("Files: ${command.files}")
                println("Auto-apply: ${command.autoApply}")
                println("Auto-commit: ${command.autoCommit}")
                println("Max retries: ${command.maxRetries}")
                
                // Show recent input history
                val recentInputs = inputHistory.getRecentInputs(3)
                if (recentInputs.size > 1) { // More than just the current input
                    println("\nRecent inputs:")
                    recentInputs.dropLast(1).forEach { input ->
                        println("  $input")
                    }
                }
                println()
            }
            
            // Initialize Git manager and retry manager
            val gitManager = GitManager(command.verbose)
            val retryManager = RetryManager(command.maxRetries, command.verbose)
            
            // Check for uncommitted changes and commit them if auto-apply is enabled
            if (command.autoApply && gitManager.isGitRepository() && gitManager.hasUncommittedChanges()) {
                outputFormatter.printSection("Detected uncommitted changes...")
                val modifiedFiles = gitManager.getModifiedFiles()
                
                if (command.verbose) {
                    println("Modified files detected:")
                    modifiedFiles.forEach { println("  - $it") }
                }
                
                outputFormatter.printSection("Committing existing changes to preserve work...")
                val preCommitMessage = "aider: save uncommitted changes before applying new edits"
                
                if (gitManager.commitUncommittedChanges(preCommitMessage)) {
                    outputFormatter.printSuccess("Existing changes committed successfully")
                } else {
                    outputFormatter.printWarning("Failed to commit existing changes - proceeding with caution")
                }
            }
            
            // Generate repository map
            outputFormatter.printSection("Analyzing repository structure...")
            val repoMapGenerator = RepoMapGenerator(command.verbose)
            val repoMap = try {
                repoMapGenerator.generateRepoMap(".", 4000) // 4k tokens as requested
            } catch (e: Exception) {
                if (command.verbose) {
                    println("Warning: Failed to generate repository map: ${e.message}")
                }
                ""
            }
            
            // Read and analyze files
            val fileContents = if (command.files.isNotEmpty()) {
                outputFormatter.printSection("Reading files...")
                fileManager.readFileContents(command.files, command.verbose)
            } else {
                emptyMap()
            }
            
            // Build context with chat history
            outputFormatter.printSection("Analyzing request...")
            val recentHistory = chatHistory.getRecentHistory(3)
            val context = buildContext(command.message, fileContents, repoMap, recentHistory, command.autoApply)
            
            // Call appropriate API with retry mechanism
            outputFormatter.printSection("Calling AI API...")
            val response = retryManager.executeWithRetry(
                context = context,
                model = command.model,
                autoApply = command.autoApply
            ) { enhancedContext ->
                when {
                    command.isVertexAIModel() -> {
                        val vertexAIClient = VertexAIClient(
                            accessToken = command.resolveVertexAIAccessToken(),
                            projectId = command.resolveVertexAIProjectId(),
                            location = command.resolveVertexAILocation(),
                            verbose = command.verbose
                        )
                        vertexAIClient.generateContent(enhancedContext, command.model)
                    }
                    command.isDeepSeekModel() -> {
                        val deepSeekClient = DeepSeekClient(command.resolveDeepSeekApiKey(), command.verbose)
                        deepSeekClient.chatCompletion(enhancedContext, command.model)
                    }
                    command.isOpenRouterModel() -> {
                        val openRouterClient = OpenRouterClient(command.resolveOpenRouterApiKey(), command.verbose)
                        openRouterClient.chatCompletion(enhancedContext, command.model)
                    }
                    command.isAnthropicModel() -> {
                        val anthropicClient = AnthropicClient(command.resolveAnthropicApiKey(), command.verbose)
                        anthropicClient.createMessage(enhancedContext, command.model)
                    }
                    else -> {
                        val openAIClient = OpenAIClient(command.resolveOpenAIApiKey(), command.verbose)
                        openAIClient.chatCompletion(enhancedContext, command.model)
                    }
                }
            }
            
            outputFormatter.printSection("Response:")
            outputFormatter.printResponse(response)
            
            // Parse and apply edits if auto-apply is enabled
            val appliedEdits = mutableListOf<String>()
            if (command.autoApply) {
                outputFormatter.printSection("Parsing edits...")
                val editBlocks = editParser.parseEdits(response)
                
                if (editBlocks.isNotEmpty()) {
                    outputFormatter.printSection("Applying edits...")
                    val editApplier = EditApplier(command.verbose)
                    val results = editApplier.applyEdits(editBlocks)
                    
                    val successfulEdits = results.filter { it.status == EditStatus.SUCCESS }
                    val failedEdits = results.filter { it.status != EditStatus.SUCCESS }
                    
                    if (successfulEdits.isNotEmpty()) {
                        outputFormatter.printSuccess("Applied ${successfulEdits.size} edit(s) successfully")
                        appliedEdits.addAll(successfulEdits.map { it.filePath })
                        
                        // Auto-commit if enabled and we're in a git repository
                        if (command.autoCommit && gitManager.isGitRepository()) {
                            outputFormatter.printSection("Committing changes...")
                            val modifiedFiles = successfulEdits.map { it.filePath }
                            
                            if (gitManager.addFiles(modifiedFiles)) {
                                val commitMessage = "aider: ${command.message}"
                                if (gitManager.commit(commitMessage)) {
                                    outputFormatter.printSuccess("Changes committed successfully")
                                } else {
                                    outputFormatter.printWarning("Failed to commit changes")
                                }
                            } else {
                                outputFormatter.printWarning("Failed to add files to git")
                            }
                        }
                    }
                    
                    if (failedEdits.isNotEmpty()) {
                        outputFormatter.printWarning("${failedEdits.size} edit(s) failed to apply")
                        failedEdits.forEach { result ->
                            println("  - ${result.filePath}: ${result.errorMessage}")
                        }
                    }
                } else {
                    outputFormatter.printWarning("No edits found in the response")
                }
            }
            
            // Add this session to chat history
            chatHistory.addEntry(
                message = command.message,
                model = command.model,
                files = command.files,
                response = response,
                appliedEdits = appliedEdits
            )
            
        } catch (e: Exception) {
            outputFormatter.printError("Error: ${e.message}")
            if (command.verbose) {
                e.printStackTrace()
            }
            throw e
        }
    }
    
    private fun buildContext(
        message: String, 
        fileContents: Map<String, String>, 
        repoMap: String, 
        recentHistory: String,
        autoApply: Boolean
    ): String {
        val context = StringBuilder()
        
        // Add recent chat history if available
        if (recentHistory.isNotBlank()) {
            context.append(recentHistory)
            context.append("\n---\n\n")
        }
        
        // Add repository map if available
        if (repoMap.isNotBlank()) {
            context.append("# Repository Context\n\n")
            context.append(repoMap)
            context.append("\n---\n\n")
        }
        
        if (fileContents.isNotEmpty()) {
            context.append("# Current Files\n\n")
            
            fileContents.forEach { (filename, content) ->
                context.append("## $filename\n")
                context.append("```\n")
                context.append(content)
                context.append("\n```\n\n")
            }
            
            context.append("---\n\n")
        }
        
        context.append("# User Request\n")
        context.append("$message\n")
        
        if (fileContents.isNotEmpty() || repoMap.isNotBlank()) {
            context.append("\nPlease analyze the repository structure and files, then provide suggestions or code changes based on the request.")
        }
        
        if (autoApply) {
            context.append("\n\n# IMPORTANT: Response Format\n")
            context.append("Please format your response using SEARCH/REPLACE blocks for any code changes. Use this exact format:\n\n")
            context.append("```\n")
            context.append("filename.kt\n")
            context.append("<<<<<<< SEARCH\n")
            context.append("exact code to search for\n")
            context.append("=======\n")
            context.append("replacement code\n")
            context.append(">>>>>>> REPLACE\n")
            context.append("```\n")
        }
        
        return context.toString()
    }
}
