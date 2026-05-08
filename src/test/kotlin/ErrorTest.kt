package com.chingis

import kotlin.test.Test
import com.chingis.error.ParseError
import com.chingis.error.RuntimeError

class ErrorTest {

    private fun run(source: String) = interpret(source.trimIndent())

    @Test fun `undefined variable throws`() {
        val ex = kotlin.runCatching { run("x = y") }.exceptionOrNull()
        assert(ex is RuntimeError) { "expected RuntimeError, got $ex" }
        assert(ex!!.message!!.contains("undefined variable 'y'"))
    }

    @Test fun `division by zero throws`() {
        val ex = kotlin.runCatching { run("x = 1 / 0") }.exceptionOrNull()
        assert(ex is RuntimeError) { "expected RuntimeError, got $ex" }
        assert(ex!!.message!!.contains("division by zero"))
    }

    @Test fun `wrong argument count throws`() {
        val ex = kotlin.runCatching { run("fun f(a) { return a }\nx = f(1, 2)") }.exceptionOrNull()
        assert(ex is RuntimeError) { "expected RuntimeError, got $ex" }
    }

    @Test fun `parse error on bad syntax`() {
        val ex = kotlin.runCatching { run("x = (1 + 2") }.exceptionOrNull()
        assert(ex is ParseError) { "expected ParseError, got $ex" }
    }
}
