package com.github.zxh0.luago.compiler.ast.exps;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.PrefixExp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParensExp extends PrefixExp {

    private Exp exp;

}
