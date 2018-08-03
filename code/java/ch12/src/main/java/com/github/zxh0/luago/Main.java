package com.github.zxh0.luago;

import com.github.zxh0.luago.api.LuaState;
import com.github.zxh0.luago.state.LuaStateImpl;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.zxh0.luago.api.LuaType.LUA_TNIL;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            byte[] data = Files.readAllBytes(Paths.get(args[0]));
            LuaState ls = new LuaStateImpl();
            ls.register("print", Main::print);
            ls.register("getmetatable", Main::getMetatable);
            ls.register("setmetatable", Main::setMetatable);
            ls.register("next", Main::next);
            ls.register("pairs", Main::pairs);
            ls.register("ipairs", Main::iPairs);
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

    private static int getMetatable(LuaState ls) {
        if (!ls.getMetatable(1)) {
            ls.pushNil();
        }
        return 1;
    }

    private static int setMetatable(LuaState ls) {
        ls.setMetatable(1);
        return 1;
    }

    private static int next(LuaState ls) {
        ls.setTop(2); /* create a 2nd argument if there isn't one */
        if (ls.next(1)) {
            return 2;
        } else {
            ls.pushNil();
            return 1;
        }
    }

    private static int pairs(LuaState ls) {
        ls.pushJavaFunction(Main::next); /* will return generator, */
        ls.pushValue(1);                 /* state, */
        ls.pushNil();
        return 3;
    }

    private static int iPairs(LuaState ls) {
        ls.pushJavaFunction(Main::iPairsAux); /* iteration function */
        ls.pushValue(1);                      /* state */
        ls.pushInteger(0);                    /* initial value */
        return 3;
    }

    private static int iPairsAux(LuaState ls) {
        long i = ls.toInteger(2) + 1;
        ls.pushInteger(i);
        return ls.getI(1, i) == LUA_TNIL ? 1 : 2;
    }

}
