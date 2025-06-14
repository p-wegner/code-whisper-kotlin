
package dev.aider

import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import dev.aider.cli.AiderCommand
import dev.aider.core.AiderCore
import dev.aider.history.ChatHistory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val parser = ArgParser("aider")
    
    // Main command arguments
    val message by parser.option(
        ArgType.String,
        shortName = "m",
        fullName = "message",
        description = "Describe the changes you want"
    )
    
    val model by parser.option(
        ArgType.String,
        fullName = "model",
        description = "Model to use (default: gpt-4)"
    )
    
    val addFiles by parser.option(
        ArgType.String,
        shortName = "f",
        fullName = "file",
        description = "Files to add to the chat session"
    ).multiple()
    
    val files by parser.argument(
        ArgType.String,
        description = "Files to include in the chat"
    ).vararg()
    
    val apiKey by parser.option(
        ArgType.String,
        fullName = "openai-api-key",
        description = "OpenAI API key (or set OPENAI_API_KEY environment variable)"
    )
    
    val anthropicApiKey by parser.option(
        ArgType.String,
        fullName = "anthropic-api-key",
        description = "Anthropic API key (or set ANTHROPIC_API_KEY environment variable)"
    )
    
    val openRouterApiKey by parser.option(
        ArgType.String,
        fullName = "openrouter-api-key",
        description = "OpenRouter API key (or set OPENROUTER_API_KEY environment variable)"
    )
    
    val deepSeekApiKey by parser.option(
        ArgType.String,
        fullName = "deepseek-api-key",
        description = "DeepSeek API key (or set DEEPSEEK_API_KEY environment variable)"
    )
    
    val verbose by parser.option(
        ArgType.Boolean,
        shortName = "v",
        fullName = "verbose",
        description = "Enable verbose output"
    ).default(false)
    
    val autoApply by parser.option(
        ArgType.Boolean,
        fullName = "auto-apply",
        description = "Automatically apply edits suggested by AI"
    ).default(false)
    
    val autoCommit by parser.option(
        ArgType.Boolean,
        fullName = "auto-commit",
        description = "Automatically commit changes after applying edits"
    ).default(false)
    
    val maxRetries by parser.option(
        ArgType.Int,
        fullName = "max-retries",
        description = "Maximum number of retries if LLM fails to follow format (default: 3)"
    ).default(3)
    
    val clearHistory by parser.option(
        ArgType.Boolean,
        fullName = "clear-history",
        description = "Clear the chat history and exit"
    ).default(false)

    try {
        parser.parse(args)
        
        // Handle clear history command
        if (clearHistory) {
            val chatHistory = ChatHistory(verbose = verbose)
            chatHistory.clearHistory()
            println("Chat history cleared successfully")
            exitProcess(0)
        }
        
        if (message == null) {
            println("Error: -m/--message is required")
            println(parser.helpFormatter.renderHelp(parser.name, parser.description))
            exitProcess(1)
        }
        
        // Combine files from -f parameter and positional arguments
        val allFiles = (addFiles + files.toList()).distinct()
        
        val command = AiderCommand(
            message = message!!,
            model = model ?: "gpt-4",
            files = allFiles,
            apiKey = apiKey,
            anthropicApiKey = anthropicApiKey,
            openRouterApiKey = openRouterApiKey,
            deepSeekApiKey = deepSeekApiKey,
            verbose = verbose,
            autoApply = autoApply,
            autoCommit = autoCommit,
            maxRetries = maxRetries
        )
        
        runBlocking {
            val aider = AiderCore()
            aider.execute(command)
        }
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        if (verbose) {
            e.printStackTrace()
        }
        exitProcess(1)
    }
}
