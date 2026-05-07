package com.chingis

fun main() {
    val source = generateSequence(::readLine).joinToString("\n")
    try {
        print(interpret(source))
    } catch (e: Exception) {
        System.err.println(e.message)
    }
}

fun interpret(source: String): String {
    val tokens = Lexer(source).tokenize()
    val ast = Parser(tokens).parseProgram()
    val interpreter = Interpreter()
    interpreter.run(ast)
    return interpreter.globals.locals()
        .filter { (_, v) -> v !is Value.Fun }
        .joinToString("\n") { (name, value) -> "$name: $value" }
}
