package com.github.zxh0.luago.compiler.ast.stats;

import com.github.zxh0.luago.compiler.ast.Stat;
import com.github.zxh0.luago.compiler.ast.exps.FuncCallExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuncCallStat extends Stat {

    private FuncCallExp exp;

    public FuncCallStat(FuncCallExp exp) {
        this.exp = exp;
    }

}
