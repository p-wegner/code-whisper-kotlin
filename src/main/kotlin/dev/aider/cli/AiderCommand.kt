package dev.aider.cli

data class AiderCommand(
    val message: String,
    val model: String,
    val files: List<String>,
    val apiKey: String?,
    val anthropicApiKey: String?,
    val openRouterApiKey: String?,
    val verbose: Boolean,
    val autoApply: Boolean = false,
    val autoCommit: Boolean = false
) {
    fun getOpenAIApiKey(): String {
        return apiKey ?: System.getenv("OPENAI_API_KEY") 
            ?: throw IllegalArgumentException("OpenAI API key not provided. Use --openai-api-key or set OPENAI_API_KEY environment variable")
    }
    
    fun getAnthropicApiKey(): String {
        return anthropicApiKey ?: System.getenv("ANTHROPIC_API_KEY") 
            ?: throw IllegalArgumentException("Anthropic API key not provided. Use --anthropic-api-key or set ANTHROPIC_API_KEY environment variable")
    }
    
    fun getOpenRouterApiKey(): String {
        return openRouterApiKey ?: System.getenv("OPENROUTER_API_KEY") 
            ?: throw IllegalArgumentException("OpenRouter API key not provided. Use --openrouter-api-key or set OPENROUTER_API_KEY environment variable")
    }
    
    fun isAnthropicModel(): Boolean {
        return model.startsWith("claude-") || model.startsWith("sonnet-") || model.startsWith("opus-") || model.startsWith("haiku-")
    }
    
    fun isOpenRouterModel(): Boolean {
        return model.contains("/") || model.startsWith("openai/") || model.startsWith("anthropic/") || 
               model.startsWith("meta-llama/") || model.startsWith("mistralai/") || model.startsWith("google/")
    }
}
