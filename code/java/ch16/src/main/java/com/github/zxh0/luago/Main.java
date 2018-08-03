package com.github.zxh0.luago;

import com.github.zxh0.luago.compiler.lexer.Lexer;
import com.github.zxh0.luago.compiler.lexer.Token;
import com.github.zxh0.luago.compiler.lexer.TokenKind;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.zxh0.luago.compiler.lexer.TokenKind.*;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            byte[] data = Files.readAllBytes(Paths.get(args[0]));
            testLexer(new String(data), args[0]);
        }
    }

    private static void testLexer(String chunk, String chunkName) {
        Lexer lexer = new Lexer(chunk, chunkName);
        for (;;) {
            Token token = lexer.nextToken();
            System.out.printf("[%2d] [%-10s] %s\n",
                    token.getLine(), kindToCategory(token.getKind()), token.getValue());
            if (token.getKind() == TOKEN_EOF) {
                break;
            }
        }
    }

    private static String kindToCategory(TokenKind kind) {
        if (kind.ordinal() < TOKEN_SEP_SEMI.ordinal()) {
            return "other";
        }
        if (kind.ordinal() <= TOKEN_SEP_RCURLY.ordinal()) {
            return "separator";
        }
        if (kind.ordinal() <= TOKEN_OP_NOT.ordinal()) {
            return "operator";
        }
        if (kind.ordinal() <= TOKEN_KW_WHILE.ordinal()) {
            return "keyword";
        }
        if (kind == TOKEN_IDENTIFIER) {
            return "identifier";
        }
        if (kind == TOKEN_NUMBER) {
            return "number";
        }
        if (kind == TOKEN_STRING) {
            return "string";
        }
        return "other";
    }

}
