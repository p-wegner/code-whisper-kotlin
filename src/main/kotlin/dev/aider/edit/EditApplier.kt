
package dev.aider.edit

import java.io.File

class EditApplier(private val verbose: Boolean = false) {
    
    fun applyEdits(editBlocks: List<EditBlock>): List<EditResult> {
        val results = mutableListOf<EditResult>()
        
        editBlocks.forEach { editBlock ->
            val result = applyEdit(editBlock)
            results.add(result)
            
            if (verbose) {
                when (result.status) {
                    EditStatus.SUCCESS -> println("✓ Applied edit to ${editBlock.filePath}")
                    EditStatus.SEARCH_NOT_FOUND -> println("✗ Search content not found in ${editBlock.filePath}")
                    EditStatus.FILE_NOT_FOUND -> println("✗ File not found: ${editBlock.filePath}")
                    EditStatus.ERROR -> println("✗ Error applying edit to ${editBlock.filePath}: ${result.errorMessage}")
                }
            }
        }
        
        return results
    }
    
    private fun applyEdit(editBlock: EditBlock): EditResult {
        try {
            val file = File(editBlock.filePath)
            
            if (!file.exists()) {
                return EditResult(editBlock.filePath, EditStatus.FILE_NOT_FOUND, "File does not exist")
            }
            
            val content = file.readText()
            val searchContent = editBlock.searchContent.trim()
            
            if (!content.contains(searchContent)) {
                return EditResult(editBlock.filePath, EditStatus.SEARCH_NOT_FOUND, "Search content not found")
            }
            
            val newContent = content.replace(searchContent, editBlock.replaceContent.trim())
            file.writeText(newContent)
            
            return EditResult(editBlock.filePath, EditStatus.SUCCESS)
            
        } catch (e: Exception) {
            return EditResult(editBlock.filePath, EditStatus.ERROR, e.message ?: "Unknown error")
        }
    }
}

data class EditResult(
    val filePath: String,
    val status: EditStatus,
    val errorMessage: String? = null
)

enum class EditStatus {
    SUCCESS,
    SEARCH_NOT_FOUND,
    FILE_NOT_FOUND,
    ERROR
}
