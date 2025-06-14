
package dev.aider.anthropic.models

import kotlinx.serialization.Serializable

@Serializable
data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    val max_tokens: Int,
    val temperature: Double = 0.7
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String
)

@Serializable
data class AnthropicResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<AnthropicContent>,
    val model: String,
    val stop_reason: String? = null,
    val stop_sequence: String? = null,
    val usage: AnthropicUsage
)

@Serializable
data class AnthropicContent(
    val type: String,
    val text: String
)

@Serializable
data class AnthropicUsage(
    val input_tokens: Int,
    val output_tokens: Int
)
