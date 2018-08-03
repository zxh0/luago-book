package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.lexer.Token;
import com.github.zxh0.luago.compiler.lexer.TokenKind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinopExp extends Exp {

    private TokenKind op; // operator
    private Exp exp1;
    private Exp exp2;

    public BinopExp(Token op, Exp exp1, Exp exp2) {
        setLine(op.getLine());
        this.exp1 = exp1;
        this.exp2 = exp2;

        if (op.getKind() == TokenKind.TOKEN_OP_MINUS) {
            this.op = TokenKind.TOKEN_OP_SUB;
        } else if (op.getKind() == TokenKind.TOKEN_OP_WAVE) {
            this.op = TokenKind.TOKEN_OP_BXOR;
        } else {
            this.op = op.getKind();
        }
    }

}
