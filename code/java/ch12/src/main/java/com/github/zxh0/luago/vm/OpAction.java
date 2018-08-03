package com.github.zxh0.luago.vm;

import com.github.zxh0.luago.api.LuaVM;

@FunctionalInterface
public interface OpAction {

    void execute(int i, LuaVM vm);

}
