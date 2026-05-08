package com.chingis.runtime

import com.chingis.ast.Expr
import com.chingis.ast.Stmt
import com.chingis.error.RuntimeError

private class ReturnSignal(val value: Value) : Throwable()

private const val MAX_CALL_DEPTH = 500

class Interpreter {
    val globals = Environment()
    private var callDepth = 0

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
        is Expr.Variable -> env.get(expr.name, expr.line, expr.col)

        is Expr.Unary -> when (expr.op) {
            "-"   -> Value.Num(-eval(expr.operand, env).toNum(0))
            "not" -> Value.Bool(!eval(expr.operand, env).isTruthy())
            else  -> throw RuntimeError(0, 0, "unknown unary op '${expr.op}'")
        }

        is Expr.Binary -> when (expr.op) {
            "and" -> Value.Bool(eval(expr.left, env).isTruthy() && eval(expr.right, env).isTruthy())
            "or"  -> Value.Bool(eval(expr.left, env).isTruthy() || eval(expr.right, env).isTruthy())
            else  -> evalBinary(expr, env)
        }

        is Expr.Call -> {
            val callee = env.get(expr.name, expr.line, expr.col)
            if (callee !is Value.Fun)
                throw RuntimeError(expr.line, expr.col, "'${expr.name}' is not a function")
            if (callee.params.size != expr.args.size)
                throw RuntimeError(expr.line, expr.col, "'${expr.name}' expects ${callee.params.size} argument(s), got ${expr.args.size}")
            if (callDepth >= MAX_CALL_DEPTH)
                throw RuntimeError(expr.line, expr.col, "call stack overflow")

            val callEnv = Environment(globals)            // functions see globals, not call-site locals
            callee.params.zip(expr.args).forEach { (param, arg) ->
                callEnv.defineLocal(param, eval(arg, env))
            }
            callDepth++
            try {
                callee.body.forEach { exec(it, callEnv) }
                Value.Num(0.0)                            // implicit return 0 if no return stmt
            } catch (r: ReturnSignal) {
                r.value
            } finally {
                callDepth--
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
                if (divisor == 0.0) throw RuntimeError(line, 0, "division by zero")
                Value.Num(l.toNum(line) / divisor)
            }
            "%"  -> {
                val divisor = r.toNum(line)
                if (divisor == 0.0) throw RuntimeError(line, 0, "modulo by zero")
                Value.Num(l.toNum(line) % divisor)
            }
            "==" -> Value.Bool(l.toNum(line) == r.toNum(line))
            "!=" -> Value.Bool(l.toNum(line) != r.toNum(line))
            "<"  -> Value.Bool(l.toNum(line) <  r.toNum(line))
            ">"  -> Value.Bool(l.toNum(line) >  r.toNum(line))
            "<=" -> Value.Bool(l.toNum(line) <= r.toNum(line))
            ">=" -> Value.Bool(l.toNum(line) >= r.toNum(line))
            else  -> throw RuntimeError(line, 0, "unknown binary op '${expr.op}'")
        }
    }
}
