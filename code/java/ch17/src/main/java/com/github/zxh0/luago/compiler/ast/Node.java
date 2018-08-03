package com.github.zxh0.luago.compiler.ast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Node {

    private int line;
    private int lastLine;

}
