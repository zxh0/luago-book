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
            ls.register("print", Main::print);
            ls.load(data, args[0], "b");
            ls.call(0, 0);
        }
    }

    private static int print(LuaState ls) {
        int nArgs = ls.getTop();
        for (int i = 1; i <= nArgs; i++) {
            if (ls.isBoolean(i)) {
                System.out.print(ls.toBoolean(i));
            } else if (ls.isString(i)) {
                System.out.print(ls.toString(i));
            } else {
                System.out.print(ls.typeName(ls.type(i)));
            }
            if (i < nArgs) {
                System.out.print("\t");
            }
        }
        System.out.println();
        return 0;
    }

}
