package com.github.zxh0.luago.api;

public interface LuaVM extends LuaState {

    int getPC();
    void addPC(int n);
    int fetch();
    void getConst(int idx);
    void getRK(int rk);

}
