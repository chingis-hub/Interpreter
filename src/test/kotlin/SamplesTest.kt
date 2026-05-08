package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals

class SamplesTest {

    private fun run(source: String) = interpret(source.trimIndent())

    @Test fun `arithmetic and variables`() {
        assertEquals("x: 2\ny: 8", run("x = 2\ny = (x + 2) * 2"))
    }

    @Test fun `if-then-else true branch`() {
        assertEquals("x: 20\ny: 100", run("x = 20\nif x > 10 then y = 100 else y = 0"))
    }

    @Test fun `if-then-else false branch`() {
        assertEquals("x: 5\ny: 0", run("x = 5\nif x > 10 then y = 100 else y = 0"))
    }

    @Test fun `while loop with if inside`() {
        assertEquals(
            "x: 3\ny: 11",
            run("x = 0\ny = 0\nwhile x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1")
        )
    }

    @Test fun `function call`() {
        assertEquals("four: 4", run("fun add(a, b) { return a + b }\nfour = add(2, 2)"))
    }

    @Test fun `recursive factorial`() {
        assertEquals(
            "a: 120",
            run("fun fact_rec(n) { if n <= 0 then return 1 else return n*fact_rec(n-1) }\na = fact_rec(5)")
        )
    }

    @Test fun `iterative factorial`() {
        assertEquals(
            "b: 120",
            run("fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }\nb = fact_iter(5)")
        )
    }
}
