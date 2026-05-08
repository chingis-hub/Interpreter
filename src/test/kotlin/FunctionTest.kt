package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionTest {

    private fun run(source: String) = interpret(source.trimIndent())

    @Test fun `function with no return uses implicit zero`() {
        assertEquals("x: 0", run("fun noop() { y = 1 }\nx = noop()"))
    }

    @Test fun `function arguments are local`() {
        assertEquals("a: 10\nresult: 20", run("""
            a = 10
            fun double(a) { return a * 2 }
            result = double(a)
        """))
    }

    @Test fun `multiple functions`() {
        assertEquals("x: 18", run("""
            fun add(a, b) { return a + b }
            fun mul(a, b) { return a * b }
            x = add(mul(2, 3), mul(3, 4))
        """))
    }

    @Test fun `fibonacci recursive`() {
        assertEquals("f: 55", run("""
            fun fib(n) { if n <= 1 then return n else return fib(n-1) + fib(n-2) }
            f = fib(10)
        """))
    }
}
