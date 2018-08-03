package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.lexer.Token;
import com.github.zxh0.luago.compiler.lexer.TokenKind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnopExp extends Exp {

    private TokenKind op; // operator
    private Exp exp;

    public UnopExp(Token op, Exp exp) {
        setLine(op.getLine());
        this.exp = exp;

        if (op.getKind() == TokenKind.TOKEN_OP_MINUS) {
            this.op = TokenKind.TOKEN_OP_UNM;
        } else if (op.getKind() == TokenKind.TOKEN_OP_WAVE) {
            this.op = TokenKind.TOKEN_OP_BNOT;
        } else {
            this.op = op.getKind();
        }
    }

}
