package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TableConstructorExp extends Exp {

    private List<Exp> keyExps;
    private List<Exp> valExps;

}
