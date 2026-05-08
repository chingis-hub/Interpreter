package com.chingis.lexer

import com.chingis.error.InterpreterError

class LexerError(line: Int, col: Int, message: String) : InterpreterError(message, line, col)

class Lexer(private val source: String) {
    private var pos = 0
    private var line = 1
    private var lineStart = 0

    private fun col() = pos - lineStart + 1

    private val keywords = mapOf(
        "fun"    to TokenType.FUN,
        "if"     to TokenType.IF,
        "then"   to TokenType.THEN,
        "else"   to TokenType.ELSE,
        "while"  to TokenType.WHILE,
        "do"     to TokenType.DO,
        "return" to TokenType.RETURN,
        "true"   to TokenType.TRUE,
        "false"  to TokenType.FALSE,
        "and"    to TokenType.AND,
        "or"     to TokenType.OR,
        "not"    to TokenType.NOT,
    )

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (pos < source.length) {
            skipSpaces()
            if (pos >= source.length) break

            val ch = source[pos]
            val tok = when {
                ch == '\n' -> {
                    val tokLine = line; val tokCol = col()
                    line++; pos++; lineStart = pos
                    Token(TokenType.NEWLINE, "\\n", tokLine, tokCol)
                }
                ch == '#' -> { skipLineComment(); continue }
                ch.isDigit() || (ch == '.' && pos + 1 < source.length && source[pos + 1].isDigit()) -> readNumber()
                ch.isLetter() || ch == '_' -> readIdentOrKeyword()
                ch == '+' -> single(TokenType.PLUS, "+")
                ch == '-' -> single(TokenType.MINUS, "-")
                ch == '*' -> single(TokenType.STAR, "*")
                ch == '/' -> single(TokenType.SLASH, "/")
                ch == '%' -> single(TokenType.PERCENT, "%")
                ch == '(' -> single(TokenType.LPAREN, "(")
                ch == ')' -> single(TokenType.RPAREN, ")")
                ch == '{' -> single(TokenType.LBRACE, "{")
                ch == '}' -> single(TokenType.RBRACE, "}")
                ch == ',' -> single(TokenType.COMMA, ",")
                ch == '=' -> if (peek(1) == '=') double(TokenType.EQEQ, "==") else single(TokenType.EQ, "=")
                ch == '!' -> if (peek(1) == '=') double(TokenType.NEQ, "!=") else throw LexerError(line, col(), "unexpected character '!'")
                ch == '<' -> if (peek(1) == '=') double(TokenType.LTEQ, "<=") else single(TokenType.LT, "<")
                ch == '>' -> if (peek(1) == '=') double(TokenType.GTEQ, ">=") else single(TokenType.GT, ">")
                else -> throw LexerError(line, col(), "unexpected character '$ch'")
            }
            tokens.add(tok)
        }
        // Collapse consecutive NEWLINEs into one
        return buildList {
            var lastWasNewline = false
            for (tok in tokens) {
                if (tok.type == TokenType.NEWLINE) {
                    if (!lastWasNewline) add(tok)
                    lastWasNewline = true
                } else {
                    add(tok)
                    lastWasNewline = false
                }
            }
            add(Token(TokenType.EOF, "", line, col()))
        }
    }

    private fun skipSpaces() {
        while (pos < source.length && source[pos] in " \t\r") pos++
    }

    private fun skipLineComment() {
        while (pos < source.length && source[pos] != '\n') pos++
    }

    private fun peek(offset: Int) = if (pos + offset < source.length) source[pos + offset] else ' '

    private fun single(type: TokenType, value: String): Token {
        val c = col(); pos++
        return Token(type, value, line, c)
    }

    private fun double(type: TokenType, value: String): Token {
        val c = col(); pos += 2
        return Token(type, value, line, c)
    }

    private fun readNumber(): Token {
        val start = pos; val c = col()
        while (pos < source.length && source[pos].isDigit()) pos++
        if (pos < source.length && source[pos] == '.' && pos + 1 < source.length && source[pos + 1].isDigit()) {
            pos++
            while (pos < source.length && source[pos].isDigit()) pos++
        }
        return Token(TokenType.NUMBER, source.substring(start, pos), line, c)
    }

    private fun readIdentOrKeyword(): Token {
        val start = pos; val c = col()
        while (pos < source.length && (source[pos].isLetterOrDigit() || source[pos] == '_')) pos++
        val text = source.substring(start, pos)
        val type = keywords[text] ?: TokenType.IDENT
        return Token(type, text, line, c)
    }
}
