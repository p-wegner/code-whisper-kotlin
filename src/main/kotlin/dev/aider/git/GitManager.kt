package dev.aider.git

import java.io.File
import java.io.IOException

class GitManager {
    private val verbose: Boolean
    
    constructor(verbose: Boolean = false) {
        this.verbose = verbose
    }
    
    fun isGitRepository(): Boolean {
        return try {
            val gitDir = File(".git")
            gitDir.exists() && gitDir.isDirectory
        } catch (e: Exception) {
            false
        }
    }
    
    fun addFiles(files: List<String>): Boolean {
        return try {
            val command = listOf("git", "add") + files
            val result = executeCommand(command)
            
            if (verbose) {
                println("Git add result: ${result.output}")
            }
            
            result.exitCode == 0
        } catch (e: Exception) {
            if (verbose) {
                println("Error adding files to git: ${e.message}")
            }
            false
        }
    }
    
    fun addAllFiles(): Boolean {
        return try {
            val command = listOf("git", "add", ".")
            val result = executeCommand(command)
            
            if (verbose) {
                println("Git add all result: ${result.output}")
            }
            
            result.exitCode == 0
        } catch (e: Exception) {
            if (verbose) {
                println("Error adding all files to git: ${e.message}")
            }
            false
        }
    }
    
    fun commit(message: String): Boolean {
        return try {
            val command = listOf("git", "commit", "-m", message)
            val result = executeCommand(command)
            
            if (verbose) {
                println("Git commit result: ${result.output}")
            }
            
            result.exitCode == 0
        } catch (e: Exception) {
            if (verbose) {
                println("Error committing to git: ${e.message}")
            }
            false
        }
    }
    
    fun getStatus(): String {
        return try {
            val command = listOf("git", "status", "--porcelain")
            val result = executeCommand(command)
            result.output
        } catch (e: Exception) {
            ""
        }
    }
    
    fun hasUncommittedChanges(): Boolean {
        return getStatus().isNotEmpty()
    }
    
    fun getModifiedFiles(): List<String> {
        return try {
            val status = getStatus()
            status.lines()
                .filter { it.isNotBlank() }
                .map { line ->
                    // Git status format: "XY filename"
                    // Take everything after the first 3 characters (status codes + space)
                    if (line.length > 3) line.substring(3).trim() else ""
                }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun commitUncommittedChanges(commitMessage: String): Boolean {
        return try {
            if (!hasUncommittedChanges()) {
                if (verbose) {
                    println("No uncommitted changes to commit")
                }
                return true
            }
            
            if (verbose) {
                println("Committing existing uncommitted changes...")
            }
            
            // Add all changes and commit
            if (addAllFiles()) {
                commit(commitMessage)
            } else {
                false
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Error committing uncommitted changes: ${e.message}")
            }
            false
        }
    }
    
    private fun executeCommand(command: List<String>): CommandResult {
        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(File("."))
        
        return try {
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            CommandResult(exitCode, output + error)
        } catch (e: IOException) {
            CommandResult(1, "Command execution failed: ${e.message}")
        }
    }
    
    data class CommandResult(
        val exitCode: Int,
        val output: String
    )
}
