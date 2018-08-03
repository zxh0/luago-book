package com.github.zxh0.luago;

import com.github.zxh0.luago.api.LuaState;
import com.github.zxh0.luago.state.LuaStateImpl;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            LuaState ls = new LuaStateImpl();
            ls.openLibs();
            ls.loadFile(args[0]);
            ls.call(0, -1);
        }
    }

}
