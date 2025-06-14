
package dev.aider.output

class OutputFormatter {
    
    fun printHeader() {
        println("🤖 Aider - AI Coding Assistant")
        println("================================")
        println()
    }
    
    fun printSection(title: String) {
        println("📁 $title")
        println("-".repeat(title.length + 3))
    }
    
    fun printResponse(response: String) {
        println()
        println(response)
        println()
    }
    
    fun printError(error: String) {
        println("❌ $error")
    }
    
    fun printSuccess(message: String) {
        println("✅ $message")
    }
    
    fun printWarning(warning: String) {
        println("⚠️  $warning")
    }
}
