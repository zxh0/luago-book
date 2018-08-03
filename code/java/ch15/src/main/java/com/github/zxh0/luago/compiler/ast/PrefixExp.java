package com.github.zxh0.luago.compiler.ast;

/*
prefixexp ::= Name |
              ‘(’ exp ‘)’ |
              prefixexp ‘[’ exp ‘]’ |
              prefixexp ‘.’ Name |
              prefixexp ‘:’ Name args |
              prefixexp args
*/
public abstract class PrefixExp extends Exp {

}
