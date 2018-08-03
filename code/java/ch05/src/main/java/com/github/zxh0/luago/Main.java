package com.github.zxh0.luago;

import com.github.zxh0.luago.api.LuaState;
import com.github.zxh0.luago.api.LuaType;
import com.github.zxh0.luago.state.LuaStateImpl;

public class Main {

    public static void main(String[] args) {
        LuaState ls = new LuaStateImpl();

        ls.pushBoolean(true);
        printStack(ls);
        ls.pushInteger(10);
        printStack(ls);
        ls.pushNil();
        printStack(ls);
        ls.pushString("hello");
        printStack(ls);
        ls.pushValue(-4);
        printStack(ls);
        ls.replace(3);
        printStack(ls);
        ls.setTop(6);
        printStack(ls);
        ls.remove(-3);
        printStack(ls);
        ls.setTop(-5);
        printStack(ls);
    }

    private static void printStack(LuaState ls) {
        int top = ls.getTop();
        for (int i = 1; i <= top; i++) {
            LuaType t = ls.type(i);
            switch (t) {
                case LUA_TBOOLEAN:
                    System.out.printf("[%b]", ls.toBoolean(i));
                    break;
                case LUA_TNUMBER:
                    if (ls.isInteger(i)) {
                        System.out.printf("[%d]", ls.toInteger(i));
                    } else {
                        System.out.printf("[%f]", ls.toNumber(i));
                    }
                    break;
                case LUA_TSTRING:
                    System.out.printf("[\"%s\"]", ls.toString(i));
                    break;
                default: // other values
                    System.out.printf("[%s]", ls.typeName(t));
                    break;
            }
        }
        System.out.println();
    }

}
