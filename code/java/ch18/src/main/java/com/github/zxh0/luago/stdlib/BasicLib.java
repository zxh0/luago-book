package com.github.zxh0.luago.stdlib;

import com.github.zxh0.luago.api.JavaFunction;
import com.github.zxh0.luago.api.LuaState;
import com.github.zxh0.luago.api.LuaType;
import com.github.zxh0.luago.api.ThreadStatus;

import java.util.HashMap;
import java.util.Map;

import static com.github.zxh0.luago.api.LuaState.LUA_MULTRET;
import static com.github.zxh0.luago.api.LuaType.*;
import static com.github.zxh0.luago.api.ThreadStatus.LUA_OK;

public class BasicLib {

    private static final Map<String, JavaFunction> baseFuncs = new HashMap<>();
    static {
        baseFuncs.put("print",        BasicLib::basePrint);
        baseFuncs.put("assert",       BasicLib::baseAssert);
        baseFuncs.put("error",        BasicLib::baseError);
        baseFuncs.put("select",       BasicLib::baseSelect);
        baseFuncs.put("ipairs",       BasicLib::baseIPairs);
        baseFuncs.put("pairs",        BasicLib::basePairs);
        baseFuncs.put("next",         BasicLib::baseNext);
        baseFuncs.put("load",         BasicLib::baseLoad);
        baseFuncs.put("loadfile",     BasicLib::baseLoadFile);
        baseFuncs.put("dofile",       BasicLib::baseDoFile);
        baseFuncs.put("pcall",        BasicLib::basePCall);
        baseFuncs.put("xpcall",       BasicLib::baseXPCall);
        baseFuncs.put("getmetatable", BasicLib::baseGetMetatable);
        baseFuncs.put("setmetatable", BasicLib::baseSetMetatable);
        baseFuncs.put("rawequal",     BasicLib::baseRawEqual);
        baseFuncs.put("rawlen",       BasicLib::baseRawLen);
        baseFuncs.put("rawget",       BasicLib::baseRawGet);
        baseFuncs.put("rawset",       BasicLib::baseRawSet);
        baseFuncs.put("type",         BasicLib::baseType);
        baseFuncs.put("tostring",     BasicLib::baseToString);
        baseFuncs.put("tonumber",     BasicLib::baseToNumber);
        /* placeholders */
        baseFuncs.put("_G",       null);
        baseFuncs.put("_VERSION", null);
    }

    public static int openBaseLib(LuaState ls) {
        /* open lib into global table */
        ls.pushGlobalTable();
        ls.setFuncs(baseFuncs, 0);
        /* set global _G */
        ls.pushValue(-1);
        ls.setField(-2, "_G");
        /* set global _VERSION */
        ls.pushString("Lua 5.3"); // todo
        ls.setField(-2, "_VERSION");
        return 1;
    }

    // print (···)
    // http://www.lua.org/manual/5.3/manual.html#pdf-print
    // lua-5.3.4/src/lbaselib.c#luaB_print()
    private static int basePrint(LuaState ls) {
        int n = ls.getTop(); /* number of arguments */
        ls.getGlobal("tostring");
        for (int i = 1; i <= n; i++) {
            ls.pushValue(-1); /* function to be called */
            ls.pushValue(i);  /* value to print */
            ls.call(1, 1);
            String s = ls.toString(-1); /* get result */
            if (s == null) {
                return ls.error2("'tostring' must return a string to 'print'");
            }
            if (i > 1) {
                System.out.print("\t");
            }
            System.out.print(s);
            ls.pop(1); /* pop result */
        }
        System.out.println();
        return 0;
    }

    // assert (v [, message])
    // http://www.lua.org/manual/5.3/manual.html#pdf-assert
    // lua-5.3.4/src/lbaselib.c#luaB_assert()
    private static int baseAssert(LuaState ls) {
        if (ls.toBoolean(1)) { /* condition is true? */
            return ls.getTop(); /* return all arguments */
        } else { /* error */
            ls.checkAny(1);                     /* there must be a condition */
            ls.remove(1);                       /* remove it */
            ls.pushString("assertion failed!"); /* default message */
            ls.setTop(1);                       /* leave only message (default if no other one) */
            return baseError(ls);               /* call 'error' */
        }
    }

    // error (message [, level])
    // http://www.lua.org/manual/5.3/manual.html#pdf-error
    // lua-5.3.4/src/lbaselib.c#luaB_error()
    private static int baseError(LuaState ls) {
        long level = ls.optInteger(2, 1);
        ls.setTop(1);
        if (ls.type(1) == LUA_TSTRING && level > 0) {
            // ls.where(level) /* add extra information */
            // ls.pushValue(1)
            // ls.concat(2)
        }
        return ls.error();
    }

    // select (index, ···)
    // http://www.lua.org/manual/5.3/manual.html#pdf-select
    // lua-5.3.4/src/lbaselib.c#luaB_select()
    private static int baseSelect(LuaState ls) {
        long n = ls.getTop();
        if (ls.type(1) == LUA_TSTRING && ls.checkString(1).equals("#")) {
            ls.pushInteger(n - 1);
            return 1;
        } else {
            long i = ls.checkInteger(1);
            if (i < 0) {
                i = n + i;
            } else if (i > n) {
                i = n;
            }
            ls.argCheck(1 <= i, 1, "index out of range");
            return (int) (n - i);
        }
    }

    // ipairs (t)
    // http://www.lua.org/manual/5.3/manual.html#pdf-ipairs
    // lua-5.3.4/src/lbaselib.c#luaB_ipairs()
    private static int baseIPairs(LuaState ls) {
        ls.checkAny(1);
        ls.pushJavaFunction(BasicLib::iPairsAux); /* iteration function */
        ls.pushValue(1);              /* state */
        ls.pushInteger(0);            /* initial value */
        return 3;
    }

    private static int iPairsAux(LuaState ls) {
        long i = ls.checkInteger(2) + 1;
        ls.pushInteger(i);
        return ls.getI(1, i) == LUA_TNIL ? 1 : 2;
    }

    // pairs (t)
    // http://www.lua.org/manual/5.3/manual.html#pdf-pairs
    // lua-5.3.4/src/lbaselib.c#luaB_pairs()
    private static int basePairs(LuaState ls) {
        ls.checkAny(1);
        if (ls.getMetafield(1, "__pairs") == LUA_TNIL) { /* no metamethod? */
            ls.pushJavaFunction(BasicLib::baseNext); /* will return generator, */
            ls.pushValue(1);             /* state, */
            ls.pushNil();
        } else {
            ls.pushValue(1); /* argument 'self' to metamethod */
            ls.call(1, 3);   /* get 3 values from metamethod */
        }
        return 3;
    }

    // next (table [, index])
    // http://www.lua.org/manual/5.3/manual.html#pdf-next
    // lua-5.3.4/src/lbaselib.c#luaB_next()
    private static int baseNext(LuaState ls) {
        ls.checkType(1, LUA_TTABLE);
        ls.setTop(2); /* create a 2nd argument if there isn't one */
        if (ls.next(1)) {
            return 2;
        } else {
            ls.pushNil();
            return 1;
        }
    }

    // load (chunk [, chunkname [, mode [, env]]])
    // http://www.lua.org/manual/5.3/manual.html#pdf-load
    // lua-5.3.4/src/lbaselib.c#luaB_load()
    private static int baseLoad(LuaState ls) {
        String chunk = ls.toString(1);
        String mode = ls.optString(3, "bt");
        int env = !ls.isNone(4) ? 4 : 0; /* 'env' index or 0 if no 'env' */
        if (chunk != null) { /* loading a string? */
            String chunkname = ls.optString(2, chunk);
            ThreadStatus status = ls.load(chunk.getBytes(), chunkname, mode);
            return loadAux(ls, status, env);
        } else { /* loading from a reader function */
            throw new RuntimeException("loading from a reader function"); // todo
        }
    }

    // lua-5.3.4/src/lbaselib.c#load_aux()
    private static int loadAux(LuaState ls, ThreadStatus status, int envIdx) {
        if (status == LUA_OK) {
            if (envIdx != 0) { /* 'env' parameter? */
                throw new RuntimeException("todo!");
            }
            return 1;
        } else { /* error (message is on top of the stack) */
            ls.pushNil();
            ls.insert(-2); /* put before error message */
            return 2;      /* return nil plus error message */
        }
    }

    // loadfile ([filename [, mode [, env]]])
    // http://www.lua.org/manual/5.3/manual.html#pdf-loadfile
    // lua-5.3.4/src/lbaselib.c#luaB_loadfile()
    private static int baseLoadFile(LuaState ls) {
        String fname = ls.optString(1, "");
        String mode = ls.optString(1, "bt");
        int env = !ls.isNone(3)? 3 : 0; /* 'env' index or 0 if no 'env' */
        ThreadStatus status = ls.loadFileX(fname, mode);
        return loadAux(ls, status, env);
    }

    // dofile ([filename])
    // http://www.lua.org/manual/5.3/manual.html#pdf-dofile
    // lua-5.3.4/src/lbaselib.c#luaB_dofile()
    private static int baseDoFile(LuaState ls) {
        String fname = ls.optString(1, "bt");
        ls.setTop(1);
        if (ls.loadFile(fname) != LUA_OK) {
            return ls.error();
        }
        ls.call(0, LUA_MULTRET);
        return ls.getTop() - 1;
    }

    // pcall (f [, arg1, ···])
    // http://www.lua.org/manual/5.3/manual.html#pdf-pcall
    private static int basePCall(LuaState ls) {
        int nArgs = ls.getTop() - 1;
        ThreadStatus status = ls.pCall(nArgs, -1, 0);
        ls.pushBoolean(status == LUA_OK);
        ls.insert(1);
        return ls.getTop();
    }

    // xpcall (f, msgh [, arg1, ···])
    // http://www.lua.org/manual/5.3/manual.html#pdf-xpcall
    private static int baseXPCall(LuaState ls) {
        throw new RuntimeException("todo!");
    }

    // getmetatable (object)
    // http://www.lua.org/manual/5.3/manual.html#pdf-getmetatable
    // lua-5.3.4/src/lbaselib.c#luaB_getmetatable()
    private static int baseGetMetatable(LuaState ls) {
        ls.checkAny(1);
        if (!ls.getMetatable(1)) {
            ls.pushNil();
            return 1; /* no metatable */
        }
        ls.getMetafield(1, "__metatable");
        return 1; /* returns either __metatable field (if present) or metatable */

    }

    // setmetatable (table, metatable)
    // http://www.lua.org/manual/5.3/manual.html#pdf-setmetatable
    // lua-5.3.4/src/lbaselib.c#luaB_setmetatable()
    private static int baseSetMetatable(LuaState ls) {
        LuaType t = ls.type(2);
        ls.checkType(1, LUA_TTABLE);
        ls.argCheck(t == LUA_TNIL || t == LUA_TTABLE, 2,
                "nil or table expected");
        if (ls.getMetafield(1, "__metatable") != LUA_TNIL) {
            return ls.error2("cannot change a protected metatable");
        }
        ls.setTop(2);
        ls.setMetatable(1);
        return 1;
    }

    // rawequal (v1, v2)
    // http://www.lua.org/manual/5.3/manual.html#pdf-rawequal
    // lua-5.3.4/src/lbaselib.c#luaB_rawequal()
    private static int baseRawEqual(LuaState ls) {
        ls.checkAny(1);
        ls.checkAny(2);
        ls.pushBoolean(ls.rawEqual(1, 2));
        return 1;
    }

    // rawlen (v)
    // http://www.lua.org/manual/5.3/manual.html#pdf-rawlen
    // lua-5.3.4/src/lbaselib.c#luaB_rawlen()
    private static int baseRawLen(LuaState ls) {
        LuaType t = ls.type(1);
        ls.argCheck(t == LUA_TTABLE || t == LUA_TSTRING, 1,
                "table or string expected");
        ls.pushInteger(ls.rawLen(1));
        return 1;
    }

    // rawget (table, index)
    // http://www.lua.org/manual/5.3/manual.html#pdf-rawget
    // lua-5.3.4/src/lbaselib.c#luaB_rawget()
    private static int baseRawGet(LuaState ls) {
        ls.checkType(1, LUA_TTABLE);
        ls.checkAny(2);
        ls.setTop(2);
        ls.rawGet(1);
        return 1;
    }

    // rawset (table, index, value)
    // http://www.lua.org/manual/5.3/manual.html#pdf-rawset
    // lua-5.3.4/src/lbaselib.c#luaB_rawset()
    private static int baseRawSet(LuaState ls) {
        ls.checkType(1, LUA_TTABLE);
        ls.checkAny(2);
        ls.checkAny(3);
        ls.setTop(3);
        ls.rawSet(1);
        return 1;
    }

    // type (v)
    // http://www.lua.org/manual/5.3/manual.html#pdf-type
    // lua-5.3.4/src/lbaselib.c#luaB_type()
    private static int baseType(LuaState ls) {
        LuaType t = ls.type(1);
        ls.argCheck(t != LUA_TNONE, 1, "value expected");
        ls.pushString(ls.typeName(t));
        return 1;
    }

    // tostring (v)
    // http://www.lua.org/manual/5.3/manual.html#pdf-tostring
    // lua-5.3.4/src/lbaselib.c#luaB_tostring()
    private static int baseToString(LuaState ls) {
        ls.checkAny(1);
        ls.toString2(1);
        return 1;
    }

    // tonumber (e [, base])
    // http://www.lua.org/manual/5.3/manual.html#pdf-tonumber
    // lua-5.3.4/src/lbaselib.c#luaB_tonumber()
    private static int baseToNumber(LuaState ls) {
        if (ls.isNoneOrNil(2)) { /* standard conversion? */
            ls.checkAny(1);
            if (ls.type(1) == LUA_TNUMBER) { /* already a number? */
                ls.setTop(1); /* yes; return it */
                return 1;
            } else {
                String s = ls.toString(1);
                if (s != null) {
                    if (ls.stringToNumber(s)) {
                        return 1; /* successful conversion to number */
                    } /* else not a number */
                }
            }
        } else {
            ls.checkType(1, LUA_TSTRING); /* no numbers as strings */
            String s = ls.toString(1).trim();
            int base = (int) ls.checkInteger(2);
            ls.argCheck(2 <= base && base <= 36, 2, "base out of range");
            try {
                long n = Long.parseLong(s, base);
                ls.pushInteger(n);
                return 1;
            } catch (NumberFormatException e) {
                /* else not a number */
            }
        } /* else not a number */
        ls.pushNil(); /* not a number */
        return 1;
    }

}
