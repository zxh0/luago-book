package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.PrefixExp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FuncCallExp extends PrefixExp {

    private Exp prefixExp;
    private StringExp nameExp;
    private List<Exp> args;

}
