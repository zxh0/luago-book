package com.github.zxh0.luago.compiler.lexer;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Token {

    static final Map<String, TokenKind> keywords = new HashMap<>();
    static {
        keywords.put("and",      TokenKind.TOKEN_OP_AND);
        keywords.put("break",    TokenKind.TOKEN_KW_BREAK);
        keywords.put("do",       TokenKind.TOKEN_KW_DO);
        keywords.put("else",     TokenKind.TOKEN_KW_ELSE);
        keywords.put("elseif",   TokenKind.TOKEN_KW_ELSEIF);
        keywords.put("end",      TokenKind.TOKEN_KW_END);
        keywords.put("false",    TokenKind.TOKEN_KW_FALSE);
        keywords.put("for",      TokenKind.TOKEN_KW_FOR);
        keywords.put("function", TokenKind.TOKEN_KW_FUNCTION);
        keywords.put("goto",     TokenKind.TOKEN_KW_GOTO);
        keywords.put("if",       TokenKind.TOKEN_KW_IF);
        keywords.put("in",       TokenKind.TOKEN_KW_IN);
        keywords.put("local",    TokenKind.TOKEN_KW_LOCAL);
        keywords.put("nil",      TokenKind.TOKEN_KW_NIL);
        keywords.put("not",      TokenKind.TOKEN_OP_NOT);
        keywords.put("or",       TokenKind.TOKEN_OP_OR);
        keywords.put("repeat",   TokenKind.TOKEN_KW_REPEAT);
        keywords.put("return",   TokenKind.TOKEN_KW_RETURN);
        keywords.put("then",     TokenKind.TOKEN_KW_THEN);
        keywords.put("true",     TokenKind.TOKEN_KW_TRUE);
        keywords.put("until",    TokenKind.TOKEN_KW_UNTIL);
        keywords.put("while",    TokenKind.TOKEN_KW_WHILE);
    }

    private final int line;
    private final TokenKind kind;
    private final String value;

    public Token(int line, TokenKind kind, String value) {
        this.line = line;
        this.kind = kind;
        this.value = value;
    }

}
