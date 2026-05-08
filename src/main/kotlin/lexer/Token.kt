package com.chingis.lexer

enum class TokenType {
    // Literals
    NUMBER, IDENT,

    // Operators
    PLUS, MINUS, STAR, SLASH, PERCENT,
    EQ, EQEQ, NEQ, LT, GT, LTEQ, GTEQ,

    // Punctuation
    LPAREN, RPAREN, LBRACE, RBRACE, COMMA,

    // Keywords
    FUN, IF, THEN, ELSE, WHILE, DO, RETURN, TRUE, FALSE, AND, OR, NOT,

    // Structure
    NEWLINE, EOF
}

data class Token(val type: TokenType, val value: String, val line: Int, val col: Int)
