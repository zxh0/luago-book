package com.github.zxh0.luago.compiler.ast.stats;

import com.github.zxh0.luago.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GotoStat extends Stat {

    private String name;

    public GotoStat(String name) {
        this.name = name;
    }

}
