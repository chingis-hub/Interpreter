package com.chingis.parser

import com.chingis.ast.Expr
import com.chingis.ast.Stmt
import com.chingis.error.ParseError
import com.chingis.lexer.Token
import com.chingis.lexer.TokenType

class Parser(private val tokens: List<Token>) {
    private var pos = 0

    private val current get() = tokens[pos]
    private val currentLine get() = current.line
    private val currentCol  get() = current.col

    // ── top-level entry ──────────────────────────────────────────────────────

    fun parseProgram(): List<Stmt> {
        val stmts = mutableListOf<Stmt>()
        skipNewlines()
        while (current.type != TokenType.EOF) {
            if (current.type == TokenType.FUN) {
                stmts.add(parseFunDef())
            } else {
                // one "logical line" = comma-separated stmt list until NEWLINE/EOF
                stmts.addAll(parseStmtList(setOf(TokenType.NEWLINE, TokenType.EOF)))
            }
            skipNewlines()
        }
        return stmts
    }

    // ── statement list (comma-separated) ─────────────────────────────────────
    // terminators: tokens that end the current context without being consumed here

    private fun parseStmtList(terminators: Set<TokenType>): List<Stmt> {
        val stmts = mutableListOf<Stmt>()
        while (current.type !in terminators) {
            val stmt = parseStmt(terminators)
            stmts.add(stmt)
            // after a while body is parsed, the remaining tokens may already be at a terminator
            if (current.type == TokenType.COMMA) advance() else break
        }
        return stmts
    }

    // ── single statement ─────────────────────────────────────────────────────

    private fun parseStmt(terminators: Set<TokenType>): Stmt {
        return when (current.type) {
            TokenType.IF     -> parseIf(terminators)
            TokenType.WHILE  -> parseWhile(terminators)
            TokenType.RETURN -> parseReturn()
            TokenType.IDENT  -> parseAssignOrCall()
            else -> throw ParseError(currentLine, currentCol, "unexpected token '${current.value}'")
        }
    }

    // if expr then stmt [else stmt]
    // then/else each take ONE stmt; comma and else act as branch terminators
    private fun parseIf(terminators: Set<TokenType>): Stmt.If {
        advance() // consume 'if'
        val condition = parseExpr()
        expect(TokenType.THEN, "expected 'then' after if-condition")

        val thenTerminators = terminators + setOf(TokenType.COMMA, TokenType.ELSE)
        val thenBranch = parseStmt(thenTerminators)

        val elseBranch = if (current.type == TokenType.ELSE) {
            advance()
            val elseTerminators = terminators + setOf(TokenType.COMMA)
            parseStmt(elseTerminators)
        } else null

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    // while expr do stmt_list — body consumes the rest of the current context
    private fun parseWhile(terminators: Set<TokenType>): Stmt.While {
        advance() // consume 'while'
        val condition = parseExpr()
        expect(TokenType.DO, "expected 'do' after while-condition")
        val body = parseStmtList(terminators)
        return Stmt.While(condition, body)
    }

    // return expr
    private fun parseReturn(): Stmt.Return {
        advance() // consume 'return'
        return Stmt.Return(parseExpr())
    }

    // IDENT = expr   or   IDENT(args)  as a statement
    private fun parseAssignOrCall(): Stmt {
        val name = current.value
        val line = currentLine; val col = currentCol
        advance()
        return if (current.type == TokenType.EQ) {
            advance()
            Stmt.Assign(name, parseExpr())
        } else if (current.type == TokenType.LPAREN) {
            Stmt.ExprStmt(finishCall(name, line, col))
        } else {
            throw ParseError(line, col, "expected '=' or '(' after identifier '$name'")
        }
    }

    // ── function definition ───────────────────────────────────────────────────

    private fun parseFunDef(): Stmt.FunDef {
        advance() // consume 'fun'
        val name = expectIdent("expected function name after 'fun'")
        expect(TokenType.LPAREN, "expected '(' after function name")
        val params = mutableListOf<String>()
        if (current.type != TokenType.RPAREN) {
            params.add(expectIdent("expected parameter name"))
            while (current.type == TokenType.COMMA) {
                advance()
                params.add(expectIdent("expected parameter name"))
            }
        }
        expect(TokenType.RPAREN, "expected ')' after parameters")
        expect(TokenType.LBRACE, "expected '{' before function body")
        skipNewlines()
        val body = parseStmtList(setOf(TokenType.RBRACE))
        expect(TokenType.RBRACE, "expected '}' after function body")
        return Stmt.FunDef(name, params, body)
    }

    // ── expressions ──────────────────────────────────────────────────────────

    private fun parseExpr(): Expr = parseOr()

    private fun parseOr(): Expr {
        var left = parseAnd()
        while (current.type == TokenType.OR) {
            val line = currentLine; advance()
            left = Expr.Binary("or", left, parseAnd(), line)
        }
        return left
    }

    private fun parseAnd(): Expr {
        var left = parseNot()
        while (current.type == TokenType.AND) {
            val line = currentLine; advance()
            left = Expr.Binary("and", left, parseNot(), line)
        }
        return left
    }

    private fun parseNot(): Expr {
        if (current.type == TokenType.NOT) { advance(); return Expr.Unary("not", parseNot()) }
        return parseComparison()
    }

    private fun parseComparison(): Expr {
        var left = parseAddSub()
        while (current.type in setOf(
                TokenType.EQEQ, TokenType.NEQ,
                TokenType.LT, TokenType.GT, TokenType.LTEQ, TokenType.GTEQ)) {
            val op = current.value; val line = currentLine; advance()
            left = Expr.Binary(op, left, parseAddSub(), line)
        }
        return left
    }

    private fun parseAddSub(): Expr {
        var left = parseMulDiv()
        while (current.type in setOf(TokenType.PLUS, TokenType.MINUS)) {
            val op = current.value; val line = currentLine; advance()
            left = Expr.Binary(op, left, parseMulDiv(), line)
        }
        return left
    }

    private fun parseMulDiv(): Expr {
        var left = parseUnary()
        while (current.type in setOf(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            val op = current.value; val line = currentLine; advance()
            left = Expr.Binary(op, left, parseUnary(), line)
        }
        return left
    }

    private fun parseUnary(): Expr {
        if (current.type == TokenType.MINUS) { advance(); return Expr.Unary("-", parseUnary()) }
        return parsePrimary()
    }

    private fun parsePrimary(): Expr {
        return when (current.type) {
            TokenType.NUMBER -> { val v = current.value.toDouble(); advance(); Expr.Number(v) }
            TokenType.TRUE   -> { advance(); Expr.Bool(true) }
            TokenType.FALSE  -> { advance(); Expr.Bool(false) }
            TokenType.IDENT  -> {
                val name = current.value; val line = currentLine; val col = currentCol; advance()
                if (current.type == TokenType.LPAREN) finishCall(name, line, col)
                else Expr.Variable(name, line, col)
            }
            TokenType.LPAREN -> {
                advance()
                val expr = parseExpr()
                expect(TokenType.RPAREN, "expected ')'")
                expr
            }
            else -> throw ParseError(currentLine, currentCol, "unexpected token '${current.value}' in expression")
        }
    }

    // parse argument list after the opening '(' is seen
    private fun finishCall(name: String, line: Int, col: Int): Expr.Call {
        advance() // consume '('
        val args = mutableListOf<Expr>()
        if (current.type != TokenType.RPAREN) {
            args.add(parseExpr())
            while (current.type == TokenType.COMMA) {
                advance(); args.add(parseExpr())
            }
        }
        expect(TokenType.RPAREN, "expected ')' after arguments")
        return Expr.Call(name, args, line, col)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun advance(): Token { val t = tokens[pos]; if (pos < tokens.size - 1) pos++; return t }

    private fun expect(type: TokenType, message: String): Token {
        if (current.type != type) throw ParseError(currentLine, currentCol, "$message, got '${current.value}'")
        return advance()
    }

    private fun expectIdent(message: String): String {
        if (current.type != TokenType.IDENT) throw ParseError(currentLine, currentCol, "$message, got '${current.value}'")
        val name = current.value; advance(); return name
    }

    private fun skipNewlines() { while (current.type == TokenType.NEWLINE) advance() }
}
