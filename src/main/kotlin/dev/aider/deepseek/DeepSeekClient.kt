
package dev.aider.deepseek

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
import dev.aider.deepseek.models.*

class DeepSeekClient(
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
            val request = DeepSeekRequest(
                model = model,
                messages = listOf(
                    DeepSeekMessage(
                        role = "system",
                        content = "You are Aider, an AI programming assistant. You help developers by analyzing code and providing suggestions, improvements, or solutions. Be concise but thorough."
                    ),
                    DeepSeekMessage(
                        role = "user", 
                        content = content
                    )
                ),
                temperature = 0.7,
                max_tokens = 2000
            )
            
            if (verbose) {
                println("Sending request to DeepSeek...")
            }
            
            val response: DeepSeekResponse = client.post("https://api.deepseek.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(request)
            }.body()
            
            return response.choices.firstOrNull()?.message?.content 
                ?: throw Exception("No response content received from DeepSeek")
                
        } catch (e: Exception) {
            throw Exception("DeepSeek API call failed: ${e.message}", e)
        }
    }
    
    fun close() {
        client.close()
    }
}
