package com.github.zxh0.luago.compiler.parser;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.lexer.Lexer;
import com.github.zxh0.luago.compiler.lexer.TokenKind;

public class Parser {

    public static Block parse(String chunk, String chunkName) {
        Lexer lexer = new Lexer(chunk, chunkName);
        Block block = BlockParser.parseBlock(lexer);
        lexer.nextTokenOfKind(TokenKind.TOKEN_EOF);
        return block;
    }

}
