package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.lexer.TokenKind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinopExp extends Exp {

    private TokenKind op; // operator
    private Exp exp1;
    private Exp exp2;

}
