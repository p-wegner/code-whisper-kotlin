package dev.aider.history

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class InputHistory(
    private val historyDir: String = ".aider",
    private val verbose: Boolean = false
) {
    private val inputHistoryFile: File
    private val maxHistorySize = 100
    
    init {
        // Create .aider directory if it doesn't exist
        val dir = File(historyDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        
        inputHistoryFile = File(dir, "input_history.txt")
        
        if (verbose) {
            println("Input history will be stored at: ${inputHistoryFile.absolutePath}")
        }
    }
    
    fun addInput(message: String, model: String, files: List<String>) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val filesStr = if (files.isNotEmpty()) " --files=${files.joinToString(",")}" else ""
            val entry = "[$timestamp] --model=$model$filesStr -m \"$message\"\n"
            
            // Read existing history
            val existingHistory = if (inputHistoryFile.exists()) {
                inputHistoryFile.readLines().toMutableList()
            } else {
                mutableListOf()
            }
            
            // Add new entry
            existingHistory.add(entry.trim())
            
            // Keep only the last maxHistorySize entries
            val trimmedHistory = if (existingHistory.size > maxHistorySize) {
                existingHistory.takeLast(maxHistorySize)
            } else {
                existingHistory
            }
            
            // Write back to file
            Files.write(
                Paths.get(inputHistoryFile.absolutePath),
                trimmedHistory.joinToString("\n").toByteArray(),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            
            if (verbose) {
                println("Added input to history")
            }
            
        } catch (e: Exception) {
            if (verbose) {
                println("Warning: Failed to write to input history: ${e.message}")
            }
        }
    }
    
    fun getHistory(): List<String> {
        return try {
            if (inputHistoryFile.exists()) {
                inputHistoryFile.readLines().filter { it.isNotBlank() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Warning: Failed to read input history: ${e.message}")
            }
            emptyList()
        }
    }
    
    fun getRecentInputs(count: Int = 10): List<String> {
        return getHistory().takeLast(count)
    }
    
    fun clearHistory() {
        try {
            if (inputHistoryFile.exists()) {
                inputHistoryFile.delete()
            }
            if (verbose) {
                println("Input history cleared")
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Warning: Failed to clear input history: ${e.message}")
            }
        }
    }
    
    fun searchHistory(query: String): List<String> {
        return getHistory().filter { entry ->
            entry.lowercase().contains(query.lowercase())
        }
    }
    
    fun getLastInput(): String? {
        val history = getHistory()
        return if (history.isNotEmpty()) {
            history.last()
        } else {
            null
        }
    }
}
