
// Example Kotlin file for testing Aider
package dev.example

class Calculator {
    fun add(a: Int, b: Int): Int {
        return a + b
    }
    
    fun subtract(a: Int, b: Int): Int {
        return a - b
    }
}

fun main() {
    val calc = Calculator()
    println("2 + 3 = ${calc.add(2, 3)}")
    println("5 - 2 = ${calc.subtract(5, 2)}")
}
