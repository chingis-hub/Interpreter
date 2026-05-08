package com.chingis.ast

sealed class Expr {
    data class Number(val value: Double) : Expr()
    data class Bool(val value: Boolean) : Expr()
    data class Str(val value: String) : Expr()
    data class Variable(val name: String, val line: Int, val col: Int) : Expr()
    data class Unary(val op: String, val operand: Expr) : Expr()
    data class Binary(val op: String, val left: Expr, val right: Expr, val line: Int) : Expr()
    data class Call(val name: String, val args: List<Expr>, val line: Int, val col: Int) : Expr()
}

sealed class Stmt {
    data class Assign(val name: String, val value: Expr) : Stmt()
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
    data class While(val condition: Expr, val body: List<Stmt>) : Stmt()
    data class FunDef(val name: String, val params: List<String>, val body: List<Stmt>) : Stmt()
    data class Return(val value: Expr) : Stmt()
    data class ExprStmt(val expr: Expr) : Stmt()
    data class Block(val stmts: List<Stmt>) : Stmt()
}
