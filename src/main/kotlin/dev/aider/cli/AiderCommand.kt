
package dev.aider.cli

data class AiderCommand(
    val message: String,
    val model: String,
    val files: List<String>,
    val apiKey: String?,
    val anthropicApiKey: String?,
    val verbose: Boolean
) {
    fun getOpenAIApiKey(): String {
        return apiKey ?: System.getenv("OPENAI_API_KEY") 
            ?: throw IllegalArgumentException("OpenAI API key not provided. Use --openai-api-key or set OPENAI_API_KEY environment variable")
    }
    
    fun getAnthropicApiKey(): String {
        return anthropicApiKey ?: System.getenv("ANTHROPIC_API_KEY") 
            ?: throw IllegalArgumentException("Anthropic API key not provided. Use --anthropic-api-key or set ANTHROPIC_API_KEY environment variable")
    }
    
    fun isAnthropicModel(): Boolean {
        return model.startsWith("claude-") || model.startsWith("sonnet-") || model.startsWith("opus-") || model.startsWith("haiku-")
    }
}
