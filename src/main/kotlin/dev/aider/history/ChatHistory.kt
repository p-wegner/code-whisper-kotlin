
package dev.aider.history

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ChatHistory(
    private val historyDir: String = ".aider",
    private val verbose: Boolean = false
) {
    private val historyFile: File
    
    init {
        // Create .aider directory if it doesn't exist
        val dir = File(historyDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        
        historyFile = File(dir, "chat_history.md")
        
        if (verbose) {
            println("Chat history will be stored at: ${historyFile.absolutePath}")
        }
    }
    
    fun addEntry(
        message: String,
        model: String,
        files: List<String>,
        response: String,
        appliedEdits: List<String> = emptyList()
    ) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            
            val entry = buildString {
                append("\n---\n")
                append("## Session: $timestamp\n\n")
                append("**Model:** $model\n\n")
                
                if (files.isNotEmpty()) {
                    append("**Files:** ${files.joinToString(", ")}\n\n")
                }
                
                append("**User Request:**\n")
                append("$message\n\n")
                
                append("**AI Response:**\n")
                append("$response\n\n")
                
                if (appliedEdits.isNotEmpty()) {
                    append("**Applied Edits:**\n")
                    appliedEdits.forEach { edit ->
                        append("- $edit\n")
                    }
                    append("\n")
                }
            }
            
            // Append to history file
            Files.write(
                Paths.get(historyFile.absolutePath),
                entry.toByteArray(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )
            
            if (verbose) {
                println("Added entry to chat history")
            }
            
        } catch (e: Exception) {
            if (verbose) {
                println("Warning: Failed to write to chat history: ${e.message}")
            }
        }
    }
    
    fun getHistory(): String {
        return try {
            if (historyFile.exists()) {
                historyFile.readText()
            } else {
                ""
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Warning: Failed to read chat history: ${e.message}")
            }
            ""
        }
    }
    
    fun getRecentHistory(maxEntries: Int = 3): String {
        val fullHistory = getHistory()
        
        if (fullHistory.isBlank()) return ""
        
        // Split by session markers and take the last N entries
        val sessions = fullHistory.split("\n---\n").filter { it.isNotBlank() }
        val recentSessions = sessions.takeLast(maxEntries)
        
        return if (recentSessions.isNotEmpty()) {
            "# Recent Chat History\n\n" + recentSessions.joinToString("\n---\n")
        } else {
            ""
        }
    }
    
    fun clearHistory() {
        try {
            if (historyFile.exists()) {
                historyFile.delete()
            }
            if (verbose) {
                println("Chat history cleared")
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Warning: Failed to clear chat history: ${e.message}")
            }
        }
    }
    
    fun initializeHistory() {
        try {
            if (!historyFile.exists()) {
                val header = """
                    # Aider Chat History
                    
                    This file contains the conversation history between you and Aider.
                    Each session is marked with a timestamp and includes the user request,
                    AI response, and any applied edits.
                    
                """.trimIndent()
                
                Files.write(
                    Paths.get(historyFile.absolutePath),
                    header.toByteArray(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
                )
                
                if (verbose) {
                    println("Initialized chat history file")
                }
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Warning: Failed to initialize chat history: ${e.message}")
            }
        }
    }
}
