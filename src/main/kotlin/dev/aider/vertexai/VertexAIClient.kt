
package dev.aider.vertexai

import dev.aider.vertexai.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class VertexAIClient(
    private val accessToken: String,
    private val projectId: String,
    private val location: String = "us-central1",
    private val verbose: Boolean = false
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    
    suspend fun generateContent(prompt: String, model: String): String = withContext(Dispatchers.IO) {
        if (verbose) {
            println("Calling Vertex AI API with model: $model")
            println("Project: $projectId, Location: $location")
        }
        
        val request = VertexAIRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt)),
                    role = "user"
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 4096
            )
        )
        
        val requestBody = json.encodeToString(request)
        
        if (verbose) {
            println("Request body: $requestBody")
        }
        
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://$location-aiplatform.googleapis.com/v1/projects/$projectId/locations/$location/publishers/google/models/$model:generateContent"))
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        
        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        
        if (verbose) {
            println("Response status: ${response.statusCode()}")
            println("Response body: ${response.body()}")
        }
        
        if (response.statusCode() != 200) {
            throw RuntimeException("Vertex AI API call failed with status ${response.statusCode()}: ${response.body()}")
        }
        
        val vertexResponse = json.decodeFromString<VertexAIResponse>(response.body())
        
        return@withContext vertexResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw RuntimeException("No response content from Vertex AI")
    }
}
