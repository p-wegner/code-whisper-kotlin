
package dev.aider.openai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import dev.aider.openai.models.*

class OpenAIClient(
    private val apiKey: String,
    private val verbose: Boolean = false
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        if (verbose) {
            install(Logging) {
                level = LogLevel.INFO
            }
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }
    }
    
    suspend fun chatCompletion(content: String, model: String): String {
        try {
            val systemPrompt = """
            You are Aider, an AI programming assistant specialized in code analysis and modification. 
            
            Your capabilities include:
            - Analyzing repository structure and understanding codebase architecture
            - Providing precise code suggestions and improvements
            - Understanding relationships between files and components
            - Suggesting refactoring and optimization opportunities
            - Following best practices and coding standards
            
            When provided with repository context, use it to:
            - Understand the overall project structure and patterns
            - Identify relevant files and dependencies
            - Provide contextually appropriate suggestions
            - Maintain consistency with existing code style and architecture
            
            Be thorough but concise. Focus on practical, actionable advice.
            """.trimIndent()
            
            val request = ChatCompletionRequest(
                model = model,
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = systemPrompt
                    ),
                    ChatMessage(
                        role = "user", 
                        content = content
                    )
                ),
                temperature = 0.7,
                maxTokens = 2000
            )
            
            if (verbose) {
                println("Sending request to OpenAI...")
            }
            
            val response: ChatCompletionResponse = client.post("https://api.openai.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(request)
            }.body()
            
            return response.choices.firstOrNull()?.message?.content 
                ?: throw Exception("No response content received from OpenAI")
                
        } catch (e: Exception) {
            throw Exception("OpenAI API call failed: ${e.message}", e)
        }
    }
    
    fun close() {
        client.close()
    }
}
