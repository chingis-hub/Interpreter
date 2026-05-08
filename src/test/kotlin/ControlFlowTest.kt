package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals

class ControlFlowTest {

    private fun run(source: String) = interpret(source.trimIndent())

    @Test fun `if without else does nothing on false`() {
        assertEquals("x: 0", run("x = 0\nif false then x = 99"))
    }

    @Test fun `while never executes when condition false`() {
        assertEquals("x: 0", run("x = 0\nwhile false do x = 1"))
    }

    @Test fun `while counts to 5`() {
        assertEquals("x: 5", run("x = 0\nwhile x < 5 do x = x + 1"))
    }

    @Test fun `nested if inside while`() {
        assertEquals("x: 4\neven: 2", run("""
            x = 0
            even = 0
            while x < 4 do if x % 2 == 0 then even = x else even = even, x = x + 1
        """))
    }
}
