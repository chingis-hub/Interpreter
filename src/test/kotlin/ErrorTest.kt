package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import com.chingis.error.LexerError
import com.chingis.error.ParseError
import com.chingis.error.RuntimeError

class ErrorTest {

    private fun run(source: String) = interpret(source.trimIndent())
    private fun err(source: String) = kotlin.runCatching { run(source) }.exceptionOrNull()

    // ── type checks ───────────────────────────────────────────────────────────

    @Test fun `undefined variable throws RuntimeError`() {
        val ex = assertIs<RuntimeError>(err("x = y"))
        assert(ex.message!!.contains("undefined variable 'y'"))
    }

    @Test fun `division by zero throws RuntimeError`() {
        val ex = assertIs<RuntimeError>(err("x = 1 / 0"))
        assert(ex.message!!.contains("division by zero"))
    }

    @Test fun `wrong argument count throws RuntimeError`() {
        assertIs<RuntimeError>(err("fun f(a) { return a }\nx = f(1, 2)"))
    }

    @Test fun `bad syntax throws ParseError`() {
        assertIs<ParseError>(err("x = (1 + 2"))
    }

    @Test fun `call stack overflow throws RuntimeError`() {
        val ex = assertIs<RuntimeError>(err("fun inf() { return inf() }\ninf()"))
        assert(ex.message!!.contains("call stack overflow"))
    }

    @Test fun `unexpected character throws LexerError`() {
        assertIs<LexerError>(err("@"))
    }

    @Test fun `bare exclamation mark throws LexerError`() {
        assertIs<LexerError>(err("!"))
    }

    // ── error positions ───────────────────────────────────────────────────────

    @Test fun `LexerError carries position`() {
        // '@' is the first character: line 1, col 1
        val ex = assertIs<LexerError>(err("@"))
        assertEquals(1, ex.line)
        assertEquals(1, ex.col)
    }

    @Test fun `LexerError col points to bad character`() {
        // "x = @" — '@' is at col 5
        val ex = assertIs<LexerError>(err("x = @"))
        assertEquals(1, ex.line)
        assertEquals(5, ex.col)
    }

    @Test fun `LexerError tracks line across newlines`() {
        // second line starts with '@'
        val ex = assertIs<LexerError>(err("x\n@"))
        assertEquals(2, ex.line)
        assertEquals(1, ex.col)
    }

    @Test fun `ParseError carries position`() {
        // "x = (1 + 2" — EOF is at line 1, col 11
        val ex = assertIs<ParseError>(err("x = (1 + 2"))
        assertEquals(1, ex.line)
        assertEquals(11, ex.col)
    }

    @Test fun `RuntimeError carries position for undefined variable`() {
        // "x = y" — 'y' is at line 1, col 5
        val ex = assertIs<RuntimeError>(err("x = y"))
        assertEquals(1, ex.line)
        assertEquals(5, ex.col)
    }

    @Test fun `RuntimeError carries position for wrong argument count`() {
        // 'f' call site is at line 2, col 5
        val ex = assertIs<RuntimeError>(err("fun f(a) { return a }\nx = f(1, 2)"))
        assertEquals(2, ex.line)
        assertEquals(5, ex.col)
    }
}
