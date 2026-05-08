package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionTest {

    private fun run(source: String) = interpret(source.trimIndent())

    @Test fun `operator precedence mul before add`() {
        assertEquals("x: 7", run("x = 1 + 2 * 3"))
    }

    @Test fun `parentheses override precedence`() {
        assertEquals("x: 9", run("x = (1 + 2) * 3"))
    }

    @Test fun `unary minus`() {
        assertEquals("x: -5", run("x = -5"))
    }

    @Test fun `unary minus in expression`() {
        assertEquals("x: 3", run("x = 10 + -7"))
    }

    @Test fun `modulo operator`() {
        assertEquals("x: 1", run("x = 10 % 3"))
    }

    @Test fun `float result`() {
        assertEquals("x: 2.5", run("x = 5 / 2"))
    }

    @Test fun `integer division prints without decimal`() {
        assertEquals("x: 4", run("x = 8 / 2"))
    }

    @Test fun `not equal operator`() {
        assertEquals("x: 1", run("x = 0\nif 1 != 2 then x = 1 else x = 0"))
    }

    @Test fun `boolean true literal`() {
        assertEquals("x: 1", run("x = 0\nif true then x = 1"))
    }

    @Test fun `boolean false literal`() {
        assertEquals("x: 0", run("x = 0\nif false then x = 1 else x = 0"))
    }

    @Test fun `and operator both true`() {
        assertEquals("x: 1", run("x = 0\nif true and true then x = 1 else x = 0"))
    }

    @Test fun `or operator one true`() {
        assertEquals("x: 1", run("x = 0\nif false or true then x = 1 else x = 0"))
    }

    @Test fun `not operator`() {
        assertEquals("x: 1", run("x = 0\nif not false then x = 1 else x = 0"))
    }

    @Test fun `and short-circuits on false left`() {
        // right side would be a runtime error if evaluated
        assertEquals("x: 0", run("x = 0\nif false and y then x = 1 else x = 0"))
    }

    @Test fun `or short-circuits on true left`() {
        assertEquals("x: 1", run("x = 0\nif true or y then x = 1 else x = 0"))
    }
}
