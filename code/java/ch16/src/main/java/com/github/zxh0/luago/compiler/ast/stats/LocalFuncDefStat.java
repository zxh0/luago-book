package com.github.zxh0.luago.compiler.ast.stats;

import com.github.zxh0.luago.compiler.ast.Stat;
import com.github.zxh0.luago.compiler.ast.exps.FuncDefExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalFuncDefStat extends Stat {

    private String name;
    private FuncDefExp exp;

}
