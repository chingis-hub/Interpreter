package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import com.chingis.error.LexerError
import com.chingis.error.RuntimeError

class StringTest {

    private fun run(source: String) = interpret(source.trimIndent())
    private fun err(source: String) = kotlin.runCatching { run(source) }.exceptionOrNull()

    // ── literals ──────────────────────────────────────────────────────────────

    @Test fun `string literal`() {
        assertEquals("s: hello", run("""s = "hello""""))
    }

    @Test fun `empty string literal`() {
        assertEquals("s: ", run("s = \"\""))
    }

    // ── escape sequences ──────────────────────────────────────────────────────

    @Test fun `escaped backslash`() {
        assertEquals("""s: a\b""", run("""s = "a\\b""""))
    }

    @Test fun `escaped quote`() {
        assertEquals("""s: say "hi"""", run("""s = "say \"hi\"""""))
    }

    // ── concatenation ─────────────────────────────────────────────────────────

    @Test fun `string plus string`() {
        assertEquals("s: helloworld", run("""s = "hello" + "world""""))
    }

    @Test fun `string plus number`() {
        assertEquals("s: x=42", run("""s = "x=" + 42"""))
    }

    @Test fun `number plus string`() {
        assertEquals("s: 42 items", run("""s = 42 + " items""""))
    }

    // ── equality ──────────────────────────────────────────────────────────────

    @Test fun `equal strings`() {
        assertEquals("x: 1", run("""x = 0, if "a" == "a" then x = 1"""))
    }

    @Test fun `unequal strings`() {
        assertEquals("x: 1", run("""x = 0, if "a" != "b" then x = 1"""))
    }

    @Test fun `string does not equal number`() {
        assertEquals("x: 0", run("""x = 0, if "1" == 1 then x = 1"""))
    }

    // ── == / != fix: types compared by value, not by coercion to Double ───────

    @Test fun `true does not equal 1`() {
        assertEquals("x: 0", run("x = 0\nif true == 1 then x = 1 else x = 0"))
    }

    @Test fun `false does not equal 0`() {
        assertEquals("x: 0", run("x = 0\nif false == 0 then x = 1 else x = 0"))
    }

    @Test fun `true equals true`() {
        assertEquals("x: 1", run("x = 0\nif true == true then x = 1 else x = 0"))
    }

    // ── truthiness ────────────────────────────────────────────────────────────

    @Test fun `non-empty string is truthy`() {
        assertEquals("x: 1", run("""x = 0, if "hi" then x = 1"""))
    }

    @Test fun `empty string is falsy`() {
        assertEquals("x: 0", run("""x = 0, if "" then x = 1 else x = 0"""))
    }

    // ── error cases ───────────────────────────────────────────────────────────

    @Test fun `unterminated string throws LexerError`() {
        assertIs<LexerError>(err("""s = "hello"""))
    }

    @Test fun `string used as number throws RuntimeError`() {
        assertIs<RuntimeError>(err("""s = "a" - 1"""))
    }
}
