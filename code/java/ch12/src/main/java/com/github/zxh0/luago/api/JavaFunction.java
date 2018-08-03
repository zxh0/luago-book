package com.github.zxh0.luago.api;

@FunctionalInterface
public interface JavaFunction {

    int invoke(LuaState ls);

}
