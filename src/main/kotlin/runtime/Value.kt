package com.chingis.runtime

import com.chingis.ast.Stmt
import com.chingis.error.RuntimeError

sealed class Value {
    data class Num(val n: Double) : Value() {
        override fun toString() =
            if (n == kotlin.math.floor(n) && !n.isInfinite()) n.toLong().toString() else n.toString()
    }
    data class Bool(val b: Boolean) : Value() {
        override fun toString() = b.toString()
    }
    data class Fun(val params: List<String>, val body: List<Stmt>) : Value() {
        override fun toString() = "<function>"
    }
    class Native(val arity: Int, val body: (List<Value>, Int, Int) -> Value) : Value() {
        override fun toString() = "<native function>"
    }

    fun isTruthy() = when (this) {
        is Bool   -> b
        is Num    -> n != 0.0
        is Fun    -> true
        is Native -> true
    }

    fun toNum(line: Int, col: Int = 0): Double = when (this) {
        is Num    -> n
        is Bool   -> if (b) 1.0 else 0.0
        is Fun    -> throw RuntimeError(line, col, "cannot use a function as a number")
        is Native -> throw RuntimeError(line, col, "cannot use a function as a number")
    }
}
