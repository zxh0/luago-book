package com.github.zxh0.luago.compiler.lexer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EscaperTest {

    @Test
    public void escape() {
        assertEquals("\07|\b|\f|\n|\r|\t|\13|\\|\"|'|\n",
                escape("\\a|\\b|\\f|\\n|\\r|\\t|\\v|\\\\|\\\"|\\'|\\\n"));
        assertEquals(" foo", escape(" \\z \t \r\n foo"));
        assertEquals("C", escape("\\67"));
        assertEquals("C", escape("\\x43"));
        assertEquals("字", escape("\\u{5B57}"));
    }

    private static String escape(String s) {
        return new Escaper(s, new Lexer(s, "str")).escape();
    }

}
