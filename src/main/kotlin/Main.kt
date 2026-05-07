package com.chingis

fun main() {
    val samples = listOf(
        "x = 2\ny = (x + 2) * 2" to "x: 2\ny: 8",
        "x = 20\nif x > 10 then y = 100 else y = 0" to "x: 20\ny: 100",
        "x = 0\ny = 0\nwhile x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1" to "x: 3\ny: 11",
        "fun add(a, b) { return a + b }\nfour = add(2, 2)" to "four: 4",
        "fun fact_rec(n) { if n <= 0 then return 1 else return n*fact_rec(n-1) }\na = fact_rec(5)" to "a: 120",
        "fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }\nb = fact_iter(5)" to "b: 120",
    )

    for ((i, pair) in samples.withIndex()) {
        val (src, expected) = pair
        val actual = interpret(src)
        val ok = actual == expected
        println("Sample ${i + 1}: ${if (ok) "PASS" else "FAIL"}")
        if (!ok) {
            println("  expected : $expected")
            println("  got      : $actual")
        }
    }
}

fun interpret(source: String): String {
    val tokens = Lexer(source).tokenize()
    val ast = Parser(tokens).parseProgram()
    val interpreter = Interpreter()
    interpreter.run(ast)
    return interpreter.globals.locals()
        .filter { (_, v) -> v !is Value.Fun }
        .joinToString("\n") { (name, value) -> "$name: $value" }
}
