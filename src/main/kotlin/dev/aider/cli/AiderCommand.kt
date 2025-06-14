
package dev.aider.cli

data class AiderCommand(
    val message: String,
    val model: String,
    val files: List<String>,
    val apiKey: String?,
    val anthropicApiKey: String?,
    val openRouterApiKey: String?,
    val deepSeekApiKey: String?,
    val vertexAIAccessToken: String?,
    val vertexAIProjectId: String?,
    val vertexAILocation: String?,
    val verbose: Boolean,
    val autoApply: Boolean = false,
    val autoCommit: Boolean = false,
    val maxRetries: Int = 3
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
    
    fun getDeepSeekApiKey(): String {
        return deepSeekApiKey ?: System.getenv("DEEPSEEK_API_KEY") 
            ?: throw IllegalArgumentException("DeepSeek API key not provided. Use --deepseek-api-key or set DEEPSEEK_API_KEY environment variable")
    }
    
    fun getVertexAIAccessToken(): String {
        return vertexAIAccessToken ?: System.getenv("VERTEX_AI_ACCESS_TOKEN") 
            ?: throw IllegalArgumentException("Vertex AI access token not provided. Use --vertex-ai-access-token or set VERTEX_AI_ACCESS_TOKEN environment variable")
    }
    
    fun getVertexAIProjectId(): String {
        return vertexAIProjectId ?: System.getenv("VERTEX_AI_PROJECT_ID") 
            ?: throw IllegalArgumentException("Vertex AI project ID not provided. Use --vertex-ai-project-id or set VERTEX_AI_PROJECT_ID environment variable")
    }
    
    fun getVertexAILocation(): String {
        return vertexAILocation ?: System.getenv("VERTEX_AI_LOCATION") ?: "us-central1"
    }
    
    fun isAnthropicModel(): Boolean {
        return model.startsWith("claude-3-5-") || model.startsWith("claude-3.5-")
    }
    
    fun isOpenRouterModel(): Boolean {
        return model.contains("/") || model.startsWith("openai/") || model.startsWith("anthropic/") || 
               model.startsWith("meta-llama/") || model.startsWith("mistralai/") || model.startsWith("google/")
    }
    
    fun isDeepSeekModel(): Boolean {
        return model.startsWith("deepseek-") || model.startsWith("deepseek/")
    }
    
    fun isVertexAIModel(): Boolean {
        return model.startsWith("gemini-") || model.contains("gemini")
    }
}
