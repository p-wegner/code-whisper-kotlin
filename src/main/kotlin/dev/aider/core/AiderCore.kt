
package dev.aider.core

import dev.aider.cli.AiderCommand
import dev.aider.openai.OpenAIClient
import dev.aider.file.FileManager
import dev.aider.output.OutputFormatter

class AiderCore {
    private val fileManager = FileManager()
    private val outputFormatter = OutputFormatter()
    
    suspend fun execute(command: AiderCommand) {
        try {
            outputFormatter.printHeader()
            
            if (command.verbose) {
                println("Using model: ${command.model}")
                println("Files: ${command.files}")
                println()
            }
            
            // Read and analyze files
            val fileContents = if (command.files.isNotEmpty()) {
                outputFormatter.printSection("Reading files...")
                fileManager.readFileContents(command.files, command.verbose)
            } else {
                emptyMap()
            }
            
            // Initialize OpenAI client
            val openAIClient = OpenAIClient(command.getApiKey(), command.verbose)
            
            // Build context and send to OpenAI
            outputFormatter.printSection("Analyzing request...")
            val context = buildContext(command.message, fileContents)
            
            outputFormatter.printSection("Calling OpenAI API...")
            val response = openAIClient.chatCompletion(context, command.model)
            
            outputFormatter.printSection("Response:")
            outputFormatter.printResponse(response)
            
        } catch (e: Exception) {
            outputFormatter.printError("Error: ${e.message}")
            if (command.verbose) {
                e.printStackTrace()
            }
            throw e
        }
    }
    
    private fun buildContext(message: String, fileContents: Map<String, String>): String {
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
        
        return context.toString()
    }
}
