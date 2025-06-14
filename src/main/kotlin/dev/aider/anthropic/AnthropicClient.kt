
package dev.aider.anthropic

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
import dev.aider.anthropic.models.*

class AnthropicClient(
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
    
    suspend fun createMessage(content: String, model: String): String {
        try {
            val request = AnthropicRequest(
                model = model,
                messages = listOf(
                    AnthropicMessage(
                        role = "user", 
                        content = content
                    )
                ),
                max_tokens = 2000,
                temperature = 0.7
            )
            
            if (verbose) {
                println("Sending request to Anthropic...")
            }
            
            val response: AnthropicResponse = client.post("https://api.anthropic.com/v1/messages") {
                contentType(ContentType.Application.Json)
                headers {
                    append("x-api-key", apiKey)
                    append("anthropic-version", "2023-06-01")
                }
                setBody(request)
            }.body()
            
            return response.content.firstOrNull()?.text 
                ?: throw Exception("No response content received from Anthropic")
                
        } catch (e: Exception) {
            throw Exception("Anthropic API call failed: ${e.message}", e)
        }
    }
    
    fun close() {
        client.close()
    }
}
