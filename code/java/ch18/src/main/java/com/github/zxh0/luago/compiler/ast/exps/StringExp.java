package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.lexer.Token;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StringExp extends Exp {

    private String str;

    public StringExp(Token token) {
        setLine(token.getLine());
        this.str = token.getValue();
    }

    public StringExp(int line, String str) {
        setLine(line);
        this.str = str;
    }

}
