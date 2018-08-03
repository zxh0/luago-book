package com.github.zxh0.luago.compiler.lexer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LexerTest {

    @Test
    public void comments() {
        String chunk = "--foo\n --[[foo]] --[==[ \nbar \r]==]";
        assertEquals(TokenKind.TOKEN_EOF,
                new Lexer(chunk, "str").nextToken().getKind());
    }

}
