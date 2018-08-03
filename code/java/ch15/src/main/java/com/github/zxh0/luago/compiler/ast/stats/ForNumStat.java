package com.github.zxh0.luago.compiler.ast.stats;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForNumStat extends Stat {

    private int lineOfFor;
    private int lineOfDo;
    private String varName;
    private Exp InitExp;
    private Exp LimitExp;
    private Exp StepExp;
    private Block block;

}
