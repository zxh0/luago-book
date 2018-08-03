package com.github.zxh0.luago.compiler.ast.stats;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ForInStat extends Stat {

    private int lineOfDo;
    private List<String> nameList;
    private List<Exp> expList;
    private Block block;

}
