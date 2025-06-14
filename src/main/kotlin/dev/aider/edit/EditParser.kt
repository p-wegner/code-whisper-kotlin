
package dev.aider.edit

import java.io.File

class EditParser {
    
    fun parseEdits(response: String): List<EditBlock> {
        val editBlocks = mutableListOf<EditBlock>()
        val lines = response.lines()
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            
            // Look for file path indicators
            if (isFilePath(line)) {
                val filePath = extractFilePath(line)
                i++
                
                // Look for SEARCH/REPLACE block
                if (i < lines.size && lines[i].contains("<<<<<<< SEARCH")) {
                    val searchContent = mutableListOf<String>()
                    i++
                    
                    // Collect search content
                    while (i < lines.size && !lines[i].contains("=======")) {
                        searchContent.add(lines[i])
                        i++
                    }
                    
                    if (i < lines.size && lines[i].contains("=======")) {
                        i++
                        val replaceContent = mutableListOf<String>()
                        
                        // Collect replace content
                        while (i < lines.size && !lines[i].contains(">>>>>>> REPLACE")) {
                            replaceContent.add(lines[i])
                            i++
                        }
                        
                        if (i < lines.size && lines[i].contains(">>>>>>> REPLACE")) {
                            editBlocks.add(
                                EditBlock(
                                    filePath = filePath,
                                    searchContent = searchContent.joinToString("\n"),
                                    replaceContent = replaceContent.joinToString("\n")
                                )
                            )
                        }
                    }
                }
            }
            i++
        }
        
        return editBlocks
    }
    
    private fun isFilePath(line: String): Boolean {
        return line.contains("/") && 
               (line.endsWith(".kt") || line.endsWith(".java") || line.endsWith(".md") || 
                line.endsWith(".txt") || line.endsWith(".json") || line.endsWith(".xml") ||
                line.endsWith(".yml") || line.endsWith(".yaml") || line.endsWith(".properties"))
    }
    
    private fun extractFilePath(line: String): String {
        return line.trim()
    }
}

data class EditBlock(
    val filePath: String,
    val searchContent: String,
    val replaceContent: String
)
