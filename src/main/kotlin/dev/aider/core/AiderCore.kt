
package dev.aider.core

import dev.aider.cli.AiderCommand
import dev.aider.openai.OpenAIClient
import dev.aider.anthropic.AnthropicClient
import dev.aider.openrouter.OpenRouterClient
import dev.aider.file.FileManager
import dev.aider.output.OutputFormatter
import dev.aider.edit.EditParser
import dev.aider.edit.EditApplier
import dev.aider.edit.EditStatus
import dev.aider.git.GitManager

class AiderCore {
    private val fileManager = FileManager()
    private val outputFormatter = OutputFormatter()
    private val editParser = EditParser()
    
    suspend fun execute(command: AiderCommand) {
        try {
            outputFormatter.printHeader()
            
            if (command.verbose) {
                println("Using model: ${command.model}")
                println("Files: ${command.files}")
                println("Auto-apply: ${command.autoApply}")
                println("Auto-commit: ${command.autoCommit}")
                println()
            }
            
            // Initialize Git manager
            val gitManager = GitManager(command.verbose)
            
            // Read and analyze files
            val fileContents = if (command.files.isNotEmpty()) {
                outputFormatter.printSection("Reading files...")
                fileManager.readFileContents(command.files, command.verbose)
            } else {
                emptyMap()
            }
            
            // Build context
            outputFormatter.printSection("Analyzing request...")
            val context = buildContext(command.message, fileContents, command.autoApply)
            
            // Call appropriate API based on model
            outputFormatter.printSection("Calling AI API...")
            val response = when {
                command.isOpenRouterModel() -> {
                    val openRouterClient = OpenRouterClient(command.getOpenRouterApiKey(), command.verbose)
                    openRouterClient.chatCompletion(context, command.model)
                }
                command.isAnthropicModel() -> {
                    val anthropicClient = AnthropicClient(command.getAnthropicApiKey(), command.verbose)
                    anthropicClient.createMessage(context, command.model)
                }
                else -> {
                    val openAIClient = OpenAIClient(command.getOpenAIApiKey(), command.verbose)
                    openAIClient.chatCompletion(context, command.model)
                }
            }
            
            outputFormatter.printSection("Response:")
            outputFormatter.printResponse(response)
            
            // Parse and apply edits if auto-apply is enabled
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
            
        } catch (e: Exception) {
            outputFormatter.printError("Error: ${e.message}")
            if (command.verbose) {
                e.printStackTrace()
            }
            throw e
        }
    }
    
    private fun buildContext(message: String, fileContents: Map<String, String>, autoApply: Boolean): String {
        val context = StringBuilder()
        
        if (fileContents.isNotEmpty()) {
            context.append("Here are the current files:\n\n")
            
            fileContents.forEach { (filename, content) ->
                context.append("=== $filename ===\n")
                context.append("$content\n\n")
            }
            
            context.append("---\n\n")
        }
        
        context.append("User request: $message\n")
        
        if (fileContents.isNotEmpty()) {
            context.append("\nPlease analyze the files and provide suggestions or code changes based on the request.")
        }
        
        if (autoApply) {
            context.append("\n\nIMPORTANT: Please format your response using SEARCH/REPLACE blocks for any code changes. Use this exact format:\n\n")
            context.append("filename.kt\n")
            context.append("<<<<<<< SEARCH\n")
            context.append("exact code to search for\n")
            context.append("=======\n")
            context.append("replacement code\n")
            context.append(">>>>>>> REPLACE\n")
        }
        
        return context.toString()
    }
}
