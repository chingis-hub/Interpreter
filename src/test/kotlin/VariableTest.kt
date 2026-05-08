package com.chingis

import kotlin.test.Test
import kotlin.test.assertEquals

class VariableTest {

    private fun run(source: String) = interpret(source.trimIndent())

    @Test fun `variables printed in declaration order`() {
        assertEquals("a: 1\nb: 2\nc: 3", run("a = 1\nb = 2\nc = 3"))
    }

    @Test fun `reassigned variable keeps original position`() {
        assertEquals("a: 99\nb: 2", run("a = 1\nb = 2\na = 99"))
    }
}
