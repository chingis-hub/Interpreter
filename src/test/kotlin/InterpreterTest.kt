package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {

    private fun run(source: String) = interpret(source.trimIndent())

    // ── sample programs from the spec ─────────────────────────────────────────

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

    // ── expressions ───────────────────────────────────────────────────────────

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

    // ── control flow ──────────────────────────────────────────────────────────

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

    // ── functions ─────────────────────────────────────────────────────────────

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

    // ── variable ordering ─────────────────────────────────────────────────────

    @Test fun `variables printed in declaration order`() {
        assertEquals("a: 1\nb: 2\nc: 3", run("a = 1\nb = 2\nc = 3"))
    }

    @Test fun `reassigned variable keeps original position`() {
        assertEquals("a: 99\nb: 2", run("a = 1\nb = 2\na = 99"))
    }

    // ── errors ────────────────────────────────────────────────────────────────

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
