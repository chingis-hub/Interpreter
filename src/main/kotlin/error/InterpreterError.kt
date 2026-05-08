package com.chingis.error

abstract class InterpreterError(message: String, val line: Int, val col: Int) : Exception(message)
