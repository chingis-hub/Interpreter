package com.chingis

import com.chingis.ast.Stmt
import com.chingis.lexer.Lexer
import com.chingis.lexer.Token
import com.chingis.parser.Parser
import com.chingis.runtime.Interpreter
import com.chingis.runtime.Value

fun main() {
    val source = generateSequence(::readLine).joinToString("\n")
    try {
        print(interpret(source))
    } catch (e: Exception) {
        System.err.println(e.message)
    }
}

fun lex(source: String): List<Token> = Lexer(source).tokenize()

fun parse(tokens: List<Token>): List<Stmt> = Parser(tokens).parseProgram()

fun evaluate(stmts: List<Stmt>): String {
    val interpreter = Interpreter()
    interpreter.run(stmts)
    return interpreter.globals.locals()
        .filter { (_, v) -> v !is Value.Fun }
        .joinToString("\n") { (name, value) -> "$name: $value" }
}

fun interpret(source: String): String = evaluate(parse(lex(source)))
