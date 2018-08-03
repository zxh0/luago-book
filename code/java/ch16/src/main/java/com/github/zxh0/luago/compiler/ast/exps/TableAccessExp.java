package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.PrefixExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableAccessExp extends PrefixExp {

    private Exp prefixExp;
    private Exp keyExp;

    public TableAccessExp(int lastLine, Exp prefixExp, Exp keyExp) {
        setLastLine(lastLine);
        this.prefixExp = prefixExp;
        this.keyExp = keyExp;
    }

}
