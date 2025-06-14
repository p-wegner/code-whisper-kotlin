
package dev.aider.retry

import dev.aider.edit.EditParser
import dev.aider.output.OutputFormatter

class RetryManager(
    private val maxRetries: Int = 3,
    private val verbose: Boolean = false
) {
    private val outputFormatter = OutputFormatter()
    private val editParser = EditParser()
    
    suspend fun executeWithRetry(
        context: String,
        model: String,
        autoApply: Boolean,
        apiCall: suspend (String) -> String
    ): String {
        var attempt = 1
        var lastResponse = ""
        var enhancedContext = context
        
        while (attempt <= maxRetries) {
            try {
                if (verbose && attempt > 1) {
                    outputFormatter.printWarning("Retry attempt $attempt/$maxRetries")
                }
                
                lastResponse = apiCall(enhancedContext)
                
                // Validate format if auto-apply is enabled
                if (autoApply) {
                    val formatValidation = validateResponseFormat(lastResponse)
                    if (formatValidation.isValid) {
                        if (verbose && attempt > 1) {
                            outputFormatter.printSuccess("LLM adhered to format on attempt $attempt")
                        }
                        return lastResponse
                    } else {
                        if (attempt < maxRetries) {
                            if (verbose) {
                                outputFormatter.printWarning("Format validation failed: ${formatValidation.reason}")
                                outputFormatter.printWarning("Enhancing prompt and retrying...")
                            }
                            enhancedContext = enhanceContextForRetry(context, formatValidation.reason, attempt)
                        } else {
                            outputFormatter.printError("Max retries reached. LLM failed to follow format: ${formatValidation.reason}")
                        }
                    }
                } else {
                    // If auto-apply is disabled, return response without format validation
                    return lastResponse
                }
                
            } catch (e: Exception) {
                if (attempt < maxRetries) {
                    if (verbose) {
                        outputFormatter.printWarning("API call failed on attempt $attempt: ${e.message}")
                        outputFormatter.printWarning("Retrying...")
                    }
                    enhancedContext = enhanceContextForRetry(context, "API call failed: ${e.message}", attempt)
                } else {
                    throw e
                }
            }
            
            attempt++
        }
        
        return lastResponse
    }
    
    private fun validateResponseFormat(response: String): FormatValidation {
        val lines = response.lines()
        var hasSearchReplaceBlock = false
        var hasValidFilePath = false
        var hasValidSearchMarker = false
        var hasValidSeparator = false
        var hasValidReplaceMarker = false
        
        // Check for basic SEARCH/REPLACE structure
        for (i in lines.indices) {
            val line = lines[i].trim()
            
            // Check for file path
            if (isFilePath(line)) {
                hasValidFilePath = true
            }
            
            // Check for SEARCH marker
            if (line.contains("<<<<<<< SEARCH")) {
                hasValidSearchMarker = true
            }
            
            // Check for separator
            if (line.contains("=======")) {
                hasValidSeparator = true
            }
            
            // Check for REPLACE marker
            if (line.contains(">>>>>>> REPLACE")) {
                hasValidReplaceMarker = true
            }
        }
        
        hasSearchReplaceBlock = hasValidSearchMarker && hasValidSeparator && hasValidReplaceMarker
        
        return when {
            !hasValidFilePath -> FormatValidation(false, "No valid file path found")
            !hasValidSearchMarker -> FormatValidation(false, "Missing '<<<<<<< SEARCH' marker")
            !hasValidSeparator -> FormatValidation(false, "Missing '=======' separator")
            !hasValidReplaceMarker -> FormatValidation(false, "Missing '>>>>>>> REPLACE' marker")
            !hasSearchReplaceBlock -> FormatValidation(false, "Incomplete SEARCH/REPLACE block structure")
            else -> {
                // Additional validation using EditParser
                val editBlocks = editParser.parseEdits(response)
                if (editBlocks.isEmpty()) {
                    FormatValidation(false, "No valid edit blocks could be parsed")
                } else {
                    FormatValidation(true, "Format is valid")
                }
            }
        }
    }
    
    private fun isFilePath(line: String): Boolean {
        return line.contains("/") && 
               (line.endsWith(".kt") || line.endsWith(".java") || line.endsWith(".md") || 
                line.endsWith(".txt") || line.endsWith(".json") || line.endsWith(".xml") ||
                line.endsWith(".yml") || line.endsWith(".yaml") || line.endsWith(".properties"))
    }
    
    private fun enhanceContextForRetry(originalContext: String, reason: String, attempt: Int): String {
        val enhancement = StringBuilder()
        
        enhancement.append("CRITICAL: Previous attempt failed - $reason\n\n")
        enhancement.append("RETRY ATTEMPT $attempt: You MUST follow the exact format specified below.\n\n")
        enhancement.append("MANDATORY FORMAT REQUIREMENTS:\n")
        enhancement.append("1. Start with the file path on its own line\n")
        enhancement.append("2. Follow with '<<<<<<< SEARCH' exactly\n")
        enhancement.append("3. Include the exact code to search for\n")
        enhancement.append("4. Add '=======' as separator\n")
        enhancement.append("5. Include the replacement code\n")
        enhancement.append("6. End with '>>>>>>> REPLACE' exactly\n\n")
        
        enhancement.append("EXAMPLE FORMAT:\n")
        enhancement.append("src/example.kt\n")
        enhancement.append("<<<<<<< SEARCH\n")
        enhancement.append("old code here\n")
        enhancement.append("=======\n")
        enhancement.append("new code here\n")
        enhancement.append(">>>>>>> REPLACE\n\n")
        
        enhancement.append("FAILURE REASON FROM PREVIOUS ATTEMPT: $reason\n\n")
        enhancement.append("---\n\n")
        enhancement.append(originalContext)
        
        return enhancement.toString()
    }
}

data class FormatValidation(
    val isValid: Boolean,
    val reason: String
)
