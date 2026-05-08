package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals
import com.chingis.error.RuntimeError
import kotlin.test.assertIs

class BuiltinsTest {

    private fun run(source: String) = interpret(source.trimIndent())

    // ── print ─────────────────────────────────────────────────────────────────

    @Test fun `print returns its argument`() {
        assertEquals("y: 42", run("y = print(42)"))
    }

    // ── abs ───────────────────────────────────────────────────────────────────

    @Test fun `abs of negative number`() {
        assertEquals("x: 5", run("x = abs(-5)"))
    }

    @Test fun `abs of positive number is unchanged`() {
        assertEquals("x: 3", run("x = abs(3)"))
    }

    // ── floor ─────────────────────────────────────────────────────────────────

    @Test fun `floor rounds down`() {
        assertEquals("x: 2", run("x = floor(2.9)"))
    }

    @Test fun `floor of whole number is unchanged`() {
        assertEquals("x: 3", run("x = floor(3)"))
    }

    // ── sqrt ──────────────────────────────────────────────────────────────────

    @Test fun `sqrt of perfect square`() {
        assertEquals("x: 4", run("x = sqrt(16)"))
    }

    @Test fun `sqrt of non-perfect square`() {
        assertEquals("x: 1.4142135623730951", run("x = sqrt(2)"))
    }

    // ── error handling ────────────────────────────────────────────────────────

    @Test fun `builtin wrong arg count throws`() {
        val ex = kotlin.runCatching { run("x = abs(1, 2)") }.exceptionOrNull()
        assertIs<RuntimeError>(ex)
    }

    // ── builtins hidden from globals output ───────────────────────────────────

    @Test fun `builtins do not appear in output`() {
        assertEquals("x: 1", run("x = 1"))
    }
}
