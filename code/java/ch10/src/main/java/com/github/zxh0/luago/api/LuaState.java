package com.github.zxh0.luago.api;

public interface LuaState {

    int LUA_MINSTACK = 20;
    int LUAI_MAXSTACK = 1000000;
    int LUA_REGISTRYINDEX = -LUAI_MAXSTACK - 1000;
    long LUA_RIDX_GLOBALS = 2;

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
    boolean isJavaFunction(int idx);
    boolean toBoolean(int idx);
    long toInteger(int idx);
    Long toIntegerX(int idx);
    double toNumber(int idx);
    Double toNumberX(int idx);
    String toString(int idx);
    JavaFunction toJavaFunction(int idx);
    /* push functions (Go -> stack); */
    void pushNil();
    void pushBoolean(boolean b);
    void pushInteger(long n);
    void pushNumber(double n);
    void pushString(String s);
    void pushJavaFunction(JavaFunction f);
    void pushJavaClosure(JavaFunction f, int n);
    void pushGlobalTable();
    /* comparison and arithmetic functions */
    void arith(ArithOp op);
    boolean compare(int idx1, int idx2, CmpOp op);
    /* get functions (Lua -> stack) */
    void newTable();
    void createTable(int nArr, int nRec);
    LuaType getTable(int idx);
    LuaType getField(int idx, String k);
    LuaType getI(int idx, long i);
    LuaType getGlobal(String name);
    /* set functions (stack -> Lua) */
    void setTable(int idx);
    void setField(int idx, String k);
    void setI(int idx, long i);
    void setGlobal(String name);
    void register(String name, JavaFunction f);
    /* 'load' and 'call' functions (load and run Lua code) */
    ThreadStatus load(byte[] chunk, String chunkName, String mode);
    void call(int nArgs, int nResults);
    /* miscellaneous functions */
    void len(int idx);
    void concat(int n);

}
