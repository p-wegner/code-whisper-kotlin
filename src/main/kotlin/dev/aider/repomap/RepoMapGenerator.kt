
package dev.aider.repomap

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class RepoMapGenerator(
    private val verbose: Boolean = false
) {
    companion object {
        private val COMMON_IGNORE_PATTERNS = setOf(
            ".git", ".idea", ".vscode", "node_modules", "build", "target", 
            "dist", "out", ".gradle", "bin", "obj", ".vs", ".DS_Store",
            "*.class", "*.jar", "*.war", "*.log", "*.tmp", "*.temp"
        )
        
        private val CODE_EXTENSIONS = setOf(
            ".kt", ".java", ".py", ".js", ".ts", ".jsx", ".tsx", ".c", ".cpp", 
            ".h", ".hpp", ".cs", ".php", ".rb", ".go", ".rs", ".swift", ".scala",
            ".clj", ".sh", ".bat", ".ps1", ".sql", ".html", ".css", ".scss",
            ".less", ".xml", ".json", ".yaml", ".yml", ".toml", ".ini", ".conf",
            ".properties", ".gradle", ".md", ".txt", ".dockerfile", ".Dockerfile"
        )
        
        private const val MAX_FILE_SIZE = 100_000 // 100KB max per file
        private const val MAX_LINES_PER_FILE = 50 // Max lines to include from each file
    }
    
    fun generateRepoMap(rootPath: String, maxTokens: Int = 4000): String {
        val root = Paths.get(rootPath).toAbsolutePath()
        
        if (verbose) {
            println("Generating repo map for: $root")
        }
        
        val fileTree = buildFileTree(root)
        val fileContents = extractKeyFileContents(root)
        
        val repoMap = StringBuilder()
        
        // Add repository structure
        repoMap.append("# Repository Structure\n\n")
        repoMap.append(fileTree)
        repoMap.append("\n\n")
        
        // Add key file contents
        if (fileContents.isNotEmpty()) {
            repoMap.append("# Key Files Overview\n\n")
            fileContents.forEach { (filePath, content) ->
                repoMap.append("## $filePath\n")
                repoMap.append("```\n")
                repoMap.append(content)
                repoMap.append("\n```\n\n")
            }
        }
        
        // Truncate if too long (rough token estimation: ~4 chars per token)
        val result = if (repoMap.length > maxTokens * 4) {
            repoMap.substring(0, maxTokens * 4) + "\n\n[... truncated for length ...]"
        } else {
            repoMap.toString()
        }
        
        if (verbose) {
            println("Generated repo map with ${result.length} characters")
        }
        
        return result
    }
    
    private fun buildFileTree(root: Path): String {
        val tree = StringBuilder()
        buildFileTreeRecursive(root, tree, "", root)
        return tree.toString()
    }
    
    private fun buildFileTreeRecursive(
        current: Path,
        tree: StringBuilder,
        prefix: String,
        root: Path,
        maxDepth: Int = 4,
        currentDepth: Int = 0
    ) {
        if (currentDepth > maxDepth) return
        
        try {
            val files = Files.list(current)
                .filter { !shouldIgnoreFile(it) }
                .sorted { a, b ->
                    // Directories first, then files
                    when {
                        Files.isDirectory(a) && !Files.isDirectory(b) -> -1
                        !Files.isDirectory(a) && Files.isDirectory(b) -> 1
                        else -> a.fileName.toString().compareTo(b.fileName.toString(), ignoreCase = true)
                    }
                }
                .toList()
            
            files.forEachIndexed { index, file ->
                val isLast = index == files.size - 1
                val currentPrefix = if (isLast) "└── " else "├── "
                val nextPrefix = if (isLast) "    " else "│   "
                
                tree.append(prefix).append(currentPrefix).append(file.fileName.toString())
                
                if (Files.isDirectory(file)) {
                    tree.append("/\n")
                    buildFileTreeRecursive(file, tree, prefix + nextPrefix, root, maxDepth, currentDepth + 1)
                } else {
                    val size = try {
                        Files.size(file)
                    } catch (e: Exception) {
                        0L
                    }
                    if (size > 0) {
                        tree.append(" (${formatFileSize(size)})")
                    }
                    tree.append("\n")
                }
            }
            
        } catch (e: Exception) {
            if (verbose) {
                println("Error reading directory $current: ${e.message}")
            }
        }
    }
    
    private fun extractKeyFileContents(root: Path): Map<String, String> {
        val keyFiles = mutableMapOf<String, String>()
        
        try {
            Files.walk(root)
                .filter { Files.isRegularFile(it) }
                .filter { !shouldIgnoreFile(it) }
                .filter { isCodeFile(it) }
                .filter { Files.size(it) < MAX_FILE_SIZE }
                .sorted { a, b ->
                    // Prioritize certain files
                    val aScore = getFileImportanceScore(a)
                    val bScore = getFileImportanceScore(b)
                    bScore.compareTo(aScore)
                }
                .limit(20) // Limit to top 20 files
                .forEach { file ->
                    try {
                        val relativePath = root.relativize(file).toString().replace('\\', '/')
                        val content = Files.readAllLines(file)
                            .take(MAX_LINES_PER_FILE)
                            .joinToString("\n")
                        
                        if (content.isNotBlank()) {
                            keyFiles[relativePath] = content
                        }
                    } catch (e: Exception) {
                        if (verbose) {
                            println("Error reading file $file: ${e.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            if (verbose) {
                println("Error walking directory tree: ${e.message}")
            }
        }
        
        return keyFiles
    }
    
    private fun shouldIgnoreFile(path: Path): Boolean {
        val fileName = path.fileName.toString()
        
        return COMMON_IGNORE_PATTERNS.any { pattern ->
            when {
                pattern.startsWith("*.") -> fileName.endsWith(pattern.substring(1))
                pattern.startsWith(".") -> fileName.startsWith(pattern)
                else -> fileName == pattern || path.toString().contains("/$pattern/") || path.toString().contains("\\$pattern\\")
            }
        }
    }
    
    private fun isCodeFile(path: Path): Boolean {
        val fileName = path.fileName.toString().lowercase()
        return CODE_EXTENSIONS.any { ext -> fileName.endsWith(ext) }
    }
    
    private fun getFileImportanceScore(path: Path): Int {
        val fileName = path.fileName.toString().lowercase()
        val pathStr = path.toString().lowercase()
        
        var score = 0
        
        // High priority files
        when {
            fileName in setOf("readme.md", "readme.txt", "readme", "main.kt", "app.kt", "application.kt") -> score += 100
            fileName.endsWith("build.gradle") || fileName.endsWith("build.gradle.kts") -> score += 90
            fileName.endsWith("pom.xml") || fileName.endsWith("package.json") -> score += 80
            fileName.endsWith("dockerfile") || fileName == "dockerfile" -> score += 70
            pathStr.contains("main") && fileName.endsWith(".kt") -> score += 60
            pathStr.contains("src") && fileName.endsWith(".kt") -> score += 50
            fileName.endsWith(".kt") -> score += 40
            fileName.endsWith(".java") -> score += 35
            pathStr.contains("test") -> score += 20
            fileName.endsWith(".md") -> score += 15
            fileName.endsWith(".json") || fileName.endsWith(".yaml") || fileName.endsWith(".yml") -> score += 10
        }
        
        // Penalty for deeply nested files
        val depth = path.nameCount
        score -= depth * 2
        
        return score
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            else -> "${bytes / (1024 * 1024)}MB"
        }
    }
}
