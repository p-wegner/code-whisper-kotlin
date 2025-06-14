
package dev.aider.file

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class FileManager {
    
    fun readFileContents(filePaths: List<String>, verbose: Boolean = false): Map<String, String> {
        val contents = mutableMapOf<String, String>()
        
        filePaths.forEach { filePath ->
            try {
                val file = File(filePath)
                if (file.exists() && file.isFile) {
                    val content = file.readText()
                    contents[filePath] = content
                    if (verbose) {
                        println("✓ Read $filePath (${content.length} characters)")
                    }
                } else {
                    if (verbose) {
                        println("✗ File not found or not a file: $filePath")
                    }
                    throw IllegalArgumentException("File not found or not accessible: $filePath")
                }
            } catch (e: Exception) {
                throw Exception("Failed to read file $filePath: ${e.message}")
            }
        }
        
        return contents
    }
    
    fun isTextFile(filePath: String): Boolean {
        return try {
            val path = Paths.get(filePath)
            val mimeType = Files.probeContentType(path)
            mimeType?.startsWith("text/") == true || 
            filePath.endsWith(".kt") ||
            filePath.endsWith(".java") ||
            filePath.endsWith(".py") ||
            filePath.endsWith(".js") ||
            filePath.endsWith(".ts") ||
            filePath.endsWith(".md") ||
            filePath.endsWith(".txt") ||
            filePath.endsWith(".json") ||
            filePath.endsWith(".xml") ||
            filePath.endsWith(".yaml") ||
            filePath.endsWith(".yml")
        } catch (e: Exception) {
            false
        }
    }
}
