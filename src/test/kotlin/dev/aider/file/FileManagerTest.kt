
package dev.aider.file

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FileManagerTest {
    
    private val fileManager = FileManager()
    
    @Test
    fun testIsTextFile() {
        assertTrue(fileManager.isTextFile("test.kt"))
        assertTrue(fileManager.isTextFile("test.java"))
        assertTrue(fileManager.isTextFile("test.py"))
        assertTrue(fileManager.isTextFile("test.js"))
        assertTrue(fileManager.isTextFile("test.md"))
        assertTrue(fileManager.isTextFile("test.json"))
        
        assertFalse(fileManager.isTextFile("test.jpg"))
        assertFalse(fileManager.isTextFile("test.png"))
        assertFalse(fileManager.isTextFile("test.bin"))
    }
}
