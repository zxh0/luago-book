package com.github.zxh0.luago;

import com.github.zxh0.luago.api.LuaState;
import com.github.zxh0.luago.state.LuaStateImpl;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            byte[] data = Files.readAllBytes(Paths.get(args[0]));
            LuaState ls = new LuaStateImpl();
            ls.load(data, args[0], "b");
            ls.call(0, 0);
        }
    }

}
