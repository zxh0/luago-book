package com.github.zxh0.luago.compiler.ast.stats;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IfStat extends Stat {

    private List<Exp> exps;
    private List<Block> blocks;

    public IfStat(List<Exp> exps, List<Block> blocks) {
        this.exps = exps;
        this.blocks = blocks;
    }

}
