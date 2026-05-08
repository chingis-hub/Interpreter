package com.chingis.error

sealed class InterpreterError(message: String, val line: Int, val col: Int) : Exception(message)

class LexerError  (line: Int, col: Int, message: String) : InterpreterError(message, line, col)
class ParseError  (line: Int, col: Int, message: String) : InterpreterError(message, line, col)
class RuntimeError(line: Int,           message: String) : InterpreterError(message, line, 0)
