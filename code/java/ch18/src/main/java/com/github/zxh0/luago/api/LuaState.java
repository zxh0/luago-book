package com.github.zxh0.luago.api;

public interface LuaState extends LuaBasicAPI, LuaAuxLib {

    int LUA_MINSTACK = 20;
    int LUAI_MAXSTACK = 1000000;
    int LUA_REGISTRYINDEX = -LUAI_MAXSTACK - 1000;
    int LUA_MULTRET = -1;
    long LUA_RIDX_GLOBALS = 2;

}
