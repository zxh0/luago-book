package com.github.zxh0.luago.api;

public interface LuaVM extends LuaState {

    void addPC(int n);
    int fetch();
    void getConst(int idx);
    void getRK(int rk);
    int registerCount();
    void loadVararg(int n);
    void loadProto(int idx);
    void closeUpvalues(int a);

}
