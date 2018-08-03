package com.github.zxh0.luago.compiler.ast.stats;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.Stat;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class LocalVarDeclStat extends Stat {

    private List<String> nameList;
    private List<Exp> expList;

    public LocalVarDeclStat(int lastLine,
                            List<String> nameList, List<Exp> expList) {
        setLastLine(lastLine);
        this.nameList = nameList != null ? nameList : Collections.emptyList();
        this.expList = expList != null ? expList : Collections.emptyList();
    }

}
