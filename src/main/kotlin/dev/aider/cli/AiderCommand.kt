
package dev.aider.cli

data class AiderCommand(
    val message: String,
    val model: String,
    val files: List<String>,
    val apiKey: String?,
    val verbose: Boolean
) {
    fun getApiKey(): String {
        return apiKey ?: System.getenv("OPENAI_API_KEY") 
            ?: throw IllegalArgumentException("OpenAI API key not provided. Use --openai-api-key or set OPENAI_API_KEY environment variable")
    }
}
