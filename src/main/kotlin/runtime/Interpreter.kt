package com.chingis.runtime

import com.chingis.ast.Expr
import com.chingis.ast.Stmt
import com.chingis.error.InterpreterError

class RuntimeError(line: Int, message: String) : InterpreterError(message, line, 0)
private class ReturnSignal(val value: Value) : Throwable()

// ── values ────────────────────────────────────────────────────────────────────

sealed class Value {
    data class Num(val n: Double) : Value() {
        override fun toString() =
            if (n == kotlin.math.floor(n) && !n.isInfinite()) n.toLong().toString() else n.toString()
    }
    data class Bool(val b: Boolean) : Value() {
        override fun toString() = b.toString()
    }
    data class Fun(val params: List<String>, val body: List<Stmt>) : Value() {
        override fun toString() = "<function>"
    }

    fun isTruthy() = when (this) {
        is Bool -> b
        is Num  -> n != 0.0
        is Fun  -> true
    }

    fun toNum(line: Int): Double = when (this) {
        is Num  -> n
        is Bool -> if (b) 1.0 else 0.0
        is Fun  -> throw RuntimeError(line, "cannot use a function as a number")
    }
}

// ── environment ───────────────────────────────────────────────────────────────

class Environment(private val parent: Environment? = null) {
    private val vars = linkedMapOf<String, Value>()

    fun get(name: String, line: Int): Value =
        vars[name] ?: parent?.get(name, line) ?: throw RuntimeError(line, "undefined variable '$name'")

    fun set(name: String, value: Value) {
        if (!assignExisting(name, value)) vars[name] = value
    }

    // walk up and update the first scope that owns 'name'; return false if not found
    private fun assignExisting(name: String, value: Value): Boolean {
        if (vars.containsKey(name)) { vars[name] = value; return true }
        return parent?.assignExisting(name, value) ?: false
    }

    fun defineLocal(name: String, value: Value) { vars[name] = value }

    // ordered entries of THIS scope only (for printing globals)
    fun locals(): List<Pair<String, Value>> = vars.entries.map { it.key to it.value }
}

// ── interpreter ───────────────────────────────────────────────────────────────

class Interpreter {
    val globals = Environment()

    fun run(stmts: List<Stmt>) = stmts.forEach { exec(it, globals) }

    // ── statement execution ───────────────────────────────────────────────────

    private fun exec(stmt: Stmt, env: Environment) {
        when (stmt) {
            is Stmt.Assign   -> env.set(stmt.name, eval(stmt.value, env))
            is Stmt.ExprStmt -> eval(stmt.expr, env)
            is Stmt.Return   -> throw ReturnSignal(eval(stmt.value, env))
            is Stmt.FunDef   -> env.set(stmt.name, Value.Fun(stmt.params, stmt.body))
            is Stmt.Block    -> stmt.stmts.forEach { exec(it, env) }

            is Stmt.If -> {
                val cond = eval(stmt.condition, env)
                if (cond.isTruthy()) exec(stmt.thenBranch, env)
                else stmt.elseBranch?.let { exec(it, env) }
            }

            is Stmt.While -> {
                while (eval(stmt.condition, env).isTruthy()) {
                    stmt.body.forEach { exec(it, env) }
                }
            }
        }
    }

    // ── expression evaluation ─────────────────────────────────────────────────

    private fun eval(expr: Expr, env: Environment): Value = when (expr) {
        is Expr.Number   -> Value.Num(expr.value)
        is Expr.Bool     -> Value.Bool(expr.value)
        is Expr.Variable -> env.get(expr.name, expr.line)

        is Expr.Unary -> when (expr.op) {
            "-"   -> Value.Num(-eval(expr.operand, env).toNum(0))
            "not" -> Value.Bool(!eval(expr.operand, env).isTruthy())
            else  -> throw RuntimeError(0, "unknown unary op '${expr.op}'")
        }

        is Expr.Binary -> evalBinary(expr, env)

        is Expr.Call -> {
            val callee = env.get(expr.name, expr.line)
            if (callee !is Value.Fun)
                throw RuntimeError(expr.line, "'${expr.name}' is not a function")
            if (callee.params.size != expr.args.size)
                throw RuntimeError(expr.line, "'${expr.name}' expects ${callee.params.size} argument(s), got ${expr.args.size}")

            val callEnv = Environment(globals)            // functions see globals, not call-site locals
            callee.params.zip(expr.args).forEach { (param, arg) ->
                callEnv.defineLocal(param, eval(arg, env))
            }
            try {
                callee.body.forEach { exec(it, callEnv) }
                Value.Num(0.0)                            // implicit return 0 if no return stmt
            } catch (r: ReturnSignal) {
                r.value
            }
        }
    }

    private fun evalBinary(expr: Expr.Binary, env: Environment): Value {
        val line = expr.line
        val l = eval(expr.left, env)
        val r = eval(expr.right, env)

        return when (expr.op) {
            "+"  -> Value.Num(l.toNum(line) + r.toNum(line))
            "-"  -> Value.Num(l.toNum(line) - r.toNum(line))
            "*"  -> Value.Num(l.toNum(line) * r.toNum(line))
            "/"  -> {
                val divisor = r.toNum(line)
                if (divisor == 0.0) throw RuntimeError(line, "division by zero")
                Value.Num(l.toNum(line) / divisor)
            }
            "%"  -> {
                val divisor = r.toNum(line)
                if (divisor == 0.0) throw RuntimeError(line, "modulo by zero")
                Value.Num(l.toNum(line) % divisor)
            }
            "==" -> Value.Bool(l.toNum(line) == r.toNum(line))
            "!=" -> Value.Bool(l.toNum(line) != r.toNum(line))
            "<"  -> Value.Bool(l.toNum(line) <  r.toNum(line))
            ">"  -> Value.Bool(l.toNum(line) >  r.toNum(line))
            "<=" -> Value.Bool(l.toNum(line) <= r.toNum(line))
            ">=" -> Value.Bool(l.toNum(line) >= r.toNum(line))
            "and" -> Value.Bool(l.isTruthy() && r.isTruthy())
            "or"  -> Value.Bool(l.isTruthy() || r.isTruthy())
            else  -> throw RuntimeError(line, "unknown binary op '${expr.op}'")
        }
    }
}
