package com.github.zxh0.luago.api;

public interface LuaState {

    /* basic stack manipulation */
    int getTop();
    int absIndex(int idx);
    boolean checkStack(int n);
    void pop(int n);
    void copy(int fromIdx, int toIdx);
    void pushValue(int idx);
    void replace(int idx);
    void insert(int idx);
    void remove(int idx);
    void rotate(int idx, int n);
    void setTop(int idx);
    /* access functions (stack -> Go); */
    String typeName(LuaType tp);
    LuaType type(int idx);
    boolean isNone(int idx);
    boolean isNil(int idx);
    boolean isNoneOrNil(int idx);
    boolean isBoolean(int idx);
    boolean isInteger(int idx);
    boolean isNumber(int idx);
    boolean isString(int idx);
    boolean isTable(int idx);
    boolean isThread(int idx);
    boolean isFunction(int idx);
    boolean toBoolean(int idx);
    long toInteger(int idx);
    Long toIntegerX(int idx);
    double toNumber(int idx);
    Double toNumberX(int idx);
    String toString(int idx);
    /* push functions (Go -> stack); */
    void pushNil();
    void pushBoolean(boolean b);
    void pushInteger(long n);
    void pushNumber(double n);
    void pushString(String s);
    /* comparison and arithmetic functions */
    void arith(ArithOp op);
    boolean compare(int idx1, int idx2, CmpOp op);
    /* get functions (Lua -> stack) */
    void newTable();
    void createTable(int nArr, int nRec);
    LuaType getTable(int idx);
    LuaType getField(int idx, String k);
    LuaType getI(int idx, long i);
    /* set functions (stack -> Lua) */
    void setTable(int idx);
    void setField(int idx, String k);
    void setI(int idx, long i);
    /* miscellaneous functions */
    void len(int idx);
    void concat(int n);

}
