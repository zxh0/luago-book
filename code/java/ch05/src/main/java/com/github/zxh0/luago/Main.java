package com.github.zxh0.luago;

import com.github.zxh0.luago.api.LuaState;
import com.github.zxh0.luago.api.LuaType;
import com.github.zxh0.luago.state.LuaStateImpl;

import static com.github.zxh0.luago.api.ArithOp.*;
import static com.github.zxh0.luago.api.CmpOp.*;
import static com.github.zxh0.luago.api.LuaType.*;

public class Main {

    public static void main(String[] args) {
        LuaState ls = new LuaStateImpl();
        ls.pushInteger(1);
        ls.pushString("2.0");
        ls.pushString("3.0");
        ls.pushNumber(4.0);
        printStack(ls);

        ls.arith(LUA_OPADD);
        printStack(ls);
        ls.arith(LUA_OPBNOT);
        printStack(ls);
        ls.len(2);
        printStack(ls);
        ls.concat(3);
        printStack(ls);
        ls.pushBoolean(ls.compare(1, 2, LUA_OPEQ));
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
