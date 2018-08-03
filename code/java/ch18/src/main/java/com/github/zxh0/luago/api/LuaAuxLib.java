package com.github.zxh0.luago.api;

import java.util.Map;

public interface LuaAuxLib {

    //type FuncReg map[string]GoFunction

    /* Error-report functions */
    int error2(String fmt, Object... a);
    int argError(int arg, String extraMsg);
    /* Argument check functions */
    void checkStack2(int sz, String msg);
    void argCheck(boolean cond, int arg, String extraMsg);
    void checkAny(int arg);
    void checkType(int arg, LuaType t);
    long checkInteger(int arg);
    double checkNumber(int arg);
    String checkString(int arg);
    long optInteger(int arg, long d);
    double optNumber(int arg, double d);
    String optString(int arg, String d);
    /* Load functions */
    boolean doFile(String filename);
    boolean doString(String str);
    ThreadStatus loadFile(String filename);
    ThreadStatus loadFileX(String filename, String mode);
    ThreadStatus loadString(String s);
    /* Other functions */
    String typeName2(int idx);
    String toString2(int idx);
    long len2(int idx);
    boolean getSubTable(int idx, String fname);
    LuaType getMetafield(int obj, String e);
    boolean callMeta(int obj, String e);
    void openLibs();
    void requireF(String modname, JavaFunction openf, boolean glb);
    void newLib(Map<String, JavaFunction> l);
    void newLibTable(Map<String, JavaFunction> l);
    void setFuncs(Map<String, JavaFunction> l, int nup);

}
