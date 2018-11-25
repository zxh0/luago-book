package com.github.zxh0.luago.compiler.lexer;

class CharUtil {

    static boolean isWhiteSpace(char c) {
        switch (c) {
            case '\t':
            case '\n':
            case 0x0B: // \v
            case '\f':
            case '\r':
            case ' ':
                return true;
        }
        return false;
    }

    static boolean isNewLine(char c) {
        return c == '\r' || c == '\n';
    }

    static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    static boolean isLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

}
