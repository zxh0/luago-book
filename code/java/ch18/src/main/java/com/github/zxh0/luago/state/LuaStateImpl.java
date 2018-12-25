package com.github.zxh0.luago.state;

import com.github.zxh0.luago.api.*;
import com.github.zxh0.luago.binchunk.BinaryChunk;
import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.binchunk.Upvalue;
import com.github.zxh0.luago.compiler.Compiler;
import com.github.zxh0.luago.number.LuaNumber;
import com.github.zxh0.luago.stdlib.BasicLib;
import com.github.zxh0.luago.vm.Instruction;
import com.github.zxh0.luago.vm.OpCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.github.zxh0.luago.api.ArithOp.*;
import static com.github.zxh0.luago.api.LuaType.*;
import static com.github.zxh0.luago.api.ThreadStatus.*;

public class LuaStateImpl implements LuaState, LuaVM {

    LuaTable registry = new LuaTable(0, 0);
    private LuaStack stack = new LuaStack();

    public LuaStateImpl() {
        registry.put(LUA_RIDX_GLOBALS, new LuaTable(0, 0));
        LuaStack stack = new LuaStack();
        stack.state = this;
        pushLuaStack(stack);
    }

    private void pushLuaStack(LuaStack newTop) {
        newTop.prev = this.stack;
        this.stack = newTop;
    }

    private void popLuaStack() {
        LuaStack top = this.stack;
        this.stack = top.prev;
        top.prev = null;
    }


    /* metatable */

    private LuaTable getMetatable(Object val) {
        if (val instanceof LuaTable) {
            return ((LuaTable) val).metatable;
        }
        String key = "_MT" + LuaValue.typeOf(val);
        Object mt = registry.get(key);
        return mt != null ? (LuaTable) mt : null;
    }

    private void setMetatable(Object val, LuaTable mt) {
        if (val instanceof LuaTable) {
            ((LuaTable) val).metatable = mt;
            return;
        }
        String key = "_MT" + LuaValue.typeOf(val);
        registry.put(key, mt);
    }

    private Object getMetafield(Object val, String fieldName) {
        LuaTable mt = getMetatable(val);
        return mt != null ? mt.get(fieldName) : null;
    }

    Object getMetamethod(Object a, Object b, String mmName) {
        Object mm = getMetafield(a, mmName);
        if (mm == null) {
            mm = getMetafield(b, mmName);
        }
        return mm;
    }

    Object callMetamethod(Object a, Object b, Object mm) {
        //stack.check(4)
        stack.push(mm);
        stack.push(a);
        stack.push(b);
        call(2, 1);
        return stack.pop();
    }

    /* basic stack manipulation */

    @Override
    public int getTop() {
        return stack.top();
    }

    @Override
    public int absIndex(int idx) {
        return stack.absIndex(idx);
    }

    @Override
    public boolean checkStack(int n) {
        return true; // TODO
    }

    @Override
    public void pop(int n) {
        for (int i = 0; i < n; i++) {
            stack.pop();
        }
    }

    @Override
    public void copy(int fromIdx, int toIdx) {
        stack.set(toIdx, stack.get(fromIdx));
    }

    @Override
    public void pushValue(int idx) {
        stack.push(stack.get(idx));
    }

    @Override
    public void replace(int idx) {
        stack.set(idx, stack.pop());
    }

    @Override
    public void insert(int idx) {
        rotate(idx, 1);
    }

    @Override
    public void remove(int idx) {
        rotate(idx, -1);
        pop(1);
    }

    @Override
    public void rotate(int idx, int n) {
        int t = stack.top() - 1;            /* end of stack segment being rotated */
        int p = stack.absIndex(idx) - 1;    /* start of segment */
        int m = n >= 0 ? t - n : p - n - 1; /* end of prefix */

        stack.reverse(p, m);     /* reverse the prefix with length 'n' */
        stack.reverse(m + 1, t); /* reverse the suffix */
        stack.reverse(p, t);     /* reverse the entire segment */
    }

    @Override
    public void setTop(int idx) {
        int newTop = stack.absIndex(idx);
        if (newTop < 0) {
            throw new RuntimeException("stack underflow!");
        }

        int n = stack.top() - newTop;
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                stack.pop();
            }
        } else if (n < 0) {
            for (int i = 0; i > n; i--) {
                stack.push(null);
            }
        }
    }

    /* access functions (stack -> Go); */

    @Override
    public String typeName(LuaType tp) {
        switch (tp) {
            case LUA_TNONE:     return "no value";
            case LUA_TNIL:      return "nil";
            case LUA_TBOOLEAN:  return "boolean";
            case LUA_TNUMBER:   return "number";
            case LUA_TSTRING:   return "string";
            case LUA_TTABLE:    return "table";
            case LUA_TFUNCTION: return "function";
            case LUA_TTHREAD:   return "thread";
            default:            return "userdata";
        }
    }

    @Override
    public LuaType type(int idx) {
        return stack.isValid(idx)
                ? LuaValue.typeOf(stack.get(idx))
                : LUA_TNONE;
    }

    @Override
    public boolean isNone(int idx) {
        return type(idx) == LUA_TNONE;
    }

    @Override
    public boolean isNil(int idx) {
        return type(idx) == LUA_TNIL;
    }

    @Override
    public boolean isNoneOrNil(int idx) {
        LuaType t = type(idx);
        return t == LUA_TNONE || t == LUA_TNIL;
    }

    @Override
    public boolean isBoolean(int idx) {
        return type(idx) == LUA_TBOOLEAN;
    }

    @Override
    public boolean isInteger(int idx) {
        return stack.get(idx) instanceof Long;
    }

    @Override
    public boolean isNumber(int idx) {
        return toNumberX(idx) != null;
    }

    @Override
    public boolean isString(int idx) {
        LuaType t = type(idx);
        return t == LUA_TSTRING || t == LUA_TNUMBER;
    }

    @Override
    public boolean isTable(int idx) {
        return type(idx) == LUA_TTABLE;
    }

    @Override
    public boolean isThread(int idx) {
        return type(idx) == LUA_TTHREAD;
    }

    @Override
    public boolean isFunction(int idx) {
        return type(idx) == LUA_TFUNCTION;
    }

    @Override
    public boolean isJavaFunction(int idx) {
        Object val = stack.get(idx);
        return val instanceof Closure
                && ((Closure) val).javaFunc != null;
    }

    @Override
    public boolean toBoolean(int idx) {
        return LuaValue.toBoolean(stack.get(idx));
    }

    @Override
    public long toInteger(int idx) {
        Long i = toIntegerX(idx);
        return i == null ? 0 : i;
    }

    @Override
    public Long toIntegerX(int idx) {
        Object val = stack.get(idx);
        return val instanceof Long ? (Long) val : null;
    }

    @Override
    public double toNumber(int idx) {
        Double n = toNumberX(idx);
        return n == null ? 0 : n;
    }

    @Override
    public Double toNumberX(int idx) {
        Object val = stack.get(idx);
        if (val instanceof Double) {
            return (Double) val;
        } else if (val instanceof Long) {
            return ((Long) val).doubleValue();
        } else {
            return null;
        }
    }

    @Override
    public String toString(int idx) {
        Object val = stack.get(idx);
        if (val instanceof String) {
            return (String) val;
        } else if (val instanceof Long || val instanceof Double) {
            return val.toString();
        } else {
            return null;
        }
    }

    @Override
    public JavaFunction toJavaFunction(int idx) {
        Object val = stack.get(idx);
        return val instanceof Closure
                ? ((Closure) val).javaFunc
                : null;
    }

    @Override
    public Object toPointer(int idx) {
        return stack.get(idx); // todo
    }

    @Override
    public int rawLen(int idx) {
        Object val = stack.get(idx);
        if (val instanceof String) {
            return ((String) val).length();
        } else if (val instanceof LuaTable) {
            return ((LuaTable) val).length();
        } else {
            return 0;
        }
    }

    /* push functions (Go -> stack); */

    @Override
    public void pushNil() {
        stack.push(null);
    }

    @Override
    public void pushBoolean(boolean b) {
        stack.push(b);
    }

    @Override
    public void pushInteger(long n) {
        stack.push(n);
    }

    @Override
    public void pushNumber(double n) {
        stack.push(n);
    }

    @Override
    public void pushString(String s) {
        stack.push(s);
    }

    @Override
    public void pushFString(String fmt, Object... a) {
        pushString(String.format(fmt, a));
    }

    @Override
    public void pushJavaFunction(JavaFunction f) {
        stack.push(new Closure(f, 0));
    }

    @Override
    public void pushJavaClosure(JavaFunction f, int n) {
        Closure closure = new Closure(f, n);
        for (int i = n; i > 0; i--) {
            Object val = stack.pop();
            closure.upvals[i-1] = new UpvalueHolder(val); // TODO
        }
        stack.push(closure);
    }

    @Override
    public void pushGlobalTable() {
        stack.push(registry.get(LUA_RIDX_GLOBALS));
    }

    /* comparison and arithmetic functions */

    @Override
    public void arith(ArithOp op) {
        Object b = stack.pop();
        Object a = op != LUA_OPUNM && op != LUA_OPBNOT ? stack.pop() : b;
        Object result = Arithmetic.arith(a, b, op, this);
        stack.push(result);
    }

    @Override
    public boolean compare(int idx1, int idx2, CmpOp op) {
        if (!stack.isValid(idx1) || !stack.isValid(idx2)) {
            return false;
        }

        Object a = stack.get(idx1);
        Object b = stack.get(idx2);
        switch (op) {
            case LUA_OPEQ: return Comparison.eq(a, b, this);
            case LUA_OPLT: return Comparison.lt(a, b, this);
            case LUA_OPLE: return Comparison.le(a, b, this);
            default: throw new RuntimeException("invalid compare op!");
        }
    }

    @Override
    public boolean rawEqual(int idx1, int idx2) {
        if (!stack.isValid(idx1) || !stack.isValid(idx2)) {
            return false;
        }

        Object a = stack.get(idx1);
        Object b = stack.get(idx2);
        return Comparison.eq(a, b, null);
    }

    /* get functions (Lua -> stack) */

    @Override
    public void newTable() {
        createTable(0, 0);
    }

    @Override
    public void createTable(int nArr, int nRec) {
        stack.push(new LuaTable(nArr, nRec));
    }

    @Override
    public LuaType getTable(int idx) {
        Object t = stack.get(idx);
        Object k = stack.pop();
        return getTable(t, k, false);
    }

    @Override
    public LuaType getField(int idx, String k) {
        Object t = stack.get(idx);
        return getTable(t, k, false);
    }

    @Override
    public LuaType getI(int idx, long i) {
        Object t = stack.get(idx);
        return getTable(t, i, false);
    }

    @Override
    public LuaType rawGet(int idx) {
        Object t = stack.get(idx);
        Object k = stack.pop();
        return getTable(t, k, true);
    }

    @Override
    public LuaType rawGetI(int idx, long i) {
        Object t = stack.get(idx);
        return getTable(t, i, true);
    }

    @Override
    public LuaType getGlobal(String name) {
        Object t = registry.get(LUA_RIDX_GLOBALS);
        return getTable(t, name, false);
    }

    @Override
    public boolean getMetatable(int idx) {
        Object val = stack.get(idx);
        Object mt = getMetatable(val);
        if (mt != null) {
            stack.push(mt);
            return true;
        } else {
            return false;
        }
    }

    private LuaType getTable(Object t, Object k, boolean raw) {
        if (t instanceof LuaTable) {
            LuaTable tbl = (LuaTable) t;
            Object v = tbl.get(k);
            if (raw || v != null || !tbl.hasMetafield("__index")) {
                stack.push(v);
                return LuaValue.typeOf(v);
            }
        }
        if (!raw) {
            Object mf = getMetafield(t, "__index");
            if (mf != null) {
                if (mf instanceof LuaTable) {
                    return getTable(mf, k, false);
                } else if (mf instanceof Closure) {
                    Object v = callMetamethod(t, k, mf);
                    stack.push(v);
                    return LuaValue.typeOf(v);
                }
            }
        }
        throw new RuntimeException("not a table!"); // todo
    }

    /* set functions (stack -> Lua) */

    @Override
    public void setTable(int idx) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        Object k = stack.pop();
        setTable(t, k, v, false);
    }

    @Override
    public void setField(int idx, String k) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, k, v, false);
    }

    @Override
    public void setI(int idx, long i) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, i, v, false);
    }

    @Override
    public void rawSet(int idx) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        Object k = stack.pop();
        setTable(t, k, v, true);
    }

    @Override
    public void rawSetI(int idx, long i) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, i, v, true);
    }

    @Override
    public void setGlobal(String name) {
        Object t = registry.get(LUA_RIDX_GLOBALS);
        Object v = stack.pop();
        setTable(t, name, v, false);
    }

    @Override
    public void register(String name, JavaFunction f) {
        pushJavaFunction(f);
        setGlobal(name);
    }

    @Override
    public void setMetatable(int idx) {
        Object val = stack.get(idx);
        Object mtVal = stack.pop();

        if (mtVal == null) {
            setMetatable(val, null);
        } else if (mtVal instanceof LuaTable) {
            setMetatable(val, (LuaTable) mtVal);
        } else {
            throw new RuntimeException("table expected!"); // todo
        }
    }

    private void setTable(Object t, Object k, Object v, boolean raw) {
        if (t instanceof LuaTable) {
            LuaTable tbl = (LuaTable) t;
            if (raw || tbl.get(k) != null || !tbl.hasMetafield("__newindex")) {
                tbl.put(k, v);
                return;
            }
        }
        if (!raw) {
            Object mf = getMetafield(t, "__newindex");
            if (mf != null) {
                if (mf instanceof LuaTable) {
                    setTable(mf, k, v, false);
                    return;
                }
                if (mf instanceof Closure) {
                    stack.push(mf);
                    stack.push(t);
                    stack.push(k);
                    stack.push(v);
                    call(3, 0);
                    return;
                }
            }
        }
        throw new RuntimeException("not a table!");
    }

    /* 'load' and 'call' functions */

    @Override
    public ThreadStatus load(byte[] chunk, String chunkName, String mode) {
        Prototype proto = BinaryChunk.isBinaryChunk(chunk)
                ? BinaryChunk.undump(chunk)
                : Compiler.compile(new String(chunk), chunkName);
        Closure closure = new Closure(proto);
        stack.push(closure);
        if (proto.getUpvalues().length > 0) {
            Object env = registry.get(LUA_RIDX_GLOBALS);
            closure.upvals[0] = new UpvalueHolder(env); // todo
        }
        return LUA_OK;
    }

    @Override
    public void call(int nArgs, int nResults) {
        Object val = stack.get(-(nArgs + 1));
        Object f = val instanceof Closure ? val : null;

        if (f == null) {
            Object mf = getMetafield(val, "__call");
            if (mf != null && mf instanceof Closure) {
                stack.push(f);
                insert(-(nArgs + 2));
                nArgs += 1;
                f = mf;
            }
        }

        if (f != null) {
            Closure c = (Closure) f;
            if (c.proto != null) {
                callLuaClosure(nArgs, nResults, c);
            } else {
                callJavaClosure(nArgs, nResults, c);
            }
        } else {
            throw new RuntimeException("not function!");
        }
    }

    private void callJavaClosure(int nArgs, int nResults, Closure c) {
        // create new lua stack
        LuaStack newStack = new LuaStack(/*nRegs+LUA_MINSTACK*/);
        newStack.state = this;
        newStack.closure = c;

        // pass args, pop func
        if (nArgs > 0) {
            newStack.pushN(stack.popN(nArgs), nArgs);
        }
        stack.pop();

        // run closure
        pushLuaStack(newStack);
        int r = c.javaFunc.invoke(this);
        popLuaStack();

        // return results
        if (nResults != 0) {
            List<Object> results = newStack.popN(r);
            //stack.check(results.size())
            stack.pushN(results, nResults);
        }
    }

    private void callLuaClosure(int nArgs, int nResults, Closure c) {
        int nRegs = c.proto.getMaxStackSize();
        int nParams = c.proto.getNumParams();
        boolean isVararg = c.proto.getIsVararg() == 1;

        // create new lua stack
        LuaStack newStack = new LuaStack(/*nRegs+LUA_MINSTACK*/);
        newStack.closure = c;

        // pass args, pop func
        List<Object> funcAndArgs = stack.popN(nArgs + 1);
        newStack.pushN(funcAndArgs.subList(1, funcAndArgs.size()), nParams);
        if (nArgs > nParams && isVararg) {
            newStack.varargs = funcAndArgs.subList(nParams + 1, funcAndArgs.size());
        }

        // run closure
        pushLuaStack(newStack);
        setTop(nRegs);
        runLuaClosure();
        popLuaStack();

        // return results
        if (nResults != 0) {
            List<Object> results = newStack.popN(newStack.top() - nRegs);
            //stack.check(results.size())
            stack.pushN(results, nResults);
        }
    }

    private void runLuaClosure() {
        for (;;) {
            int i = fetch();
            OpCode opCode = Instruction.getOpCode(i);
            opCode.getAction().execute(i, this);
            if (opCode == OpCode.RETURN) {
                break;
            }
        }
    }


    @Override
    public ThreadStatus pCall(int nArgs, int nResults, int msgh) {
        LuaStack caller = stack;
        try {
            call(nArgs, nResults);
            return LUA_OK;
        } catch (Exception e) {
            if (msgh != 0) {
                throw e;
            }
            while (stack != caller) {
                popLuaStack();
            }
            stack.push(e.getMessage()); // TODO
            return LUA_ERRRUN;
        }
    }

    /* miscellaneous functions */

    @Override
    public void len(int idx) {
        Object val = stack.get(idx);
        if (val instanceof String) {
            pushInteger(((String) val).length());
            return;
        }
        Object mm = getMetamethod(val, val, "__len");
        if (mm != null) {
            stack.push(callMetamethod(val, val, mm));
            return;
        }
        if (val instanceof LuaTable) {
            pushInteger(((LuaTable) val).length());
            return;
        }
        throw new RuntimeException("length error!");
    }

    @Override
    public void concat(int n) {
        if (n == 0) {
            stack.push("");
        } else if (n >= 2) {
            for (int i = 1; i < n; i++) {
                if (isString(-1) && isString(-2)) {
                    String s2 = toString(-1);
                    String s1 = toString(-2);
                    pop(2);
                    pushString(s1 + s2);
                    continue;
                }

                Object b = stack.pop();
                Object a = stack.pop();
                Object mm = getMetamethod(a, b, "__concat");
                if (mm != null) {
                    stack.push(callMetamethod(a, b, mm));
                    continue;
                }

                throw new RuntimeException("concatenation error!");
            }
        }
        // n == 1, do nothing
    }

    @Override
    public boolean next(int idx) {
        Object val = stack.get(idx);
        if (val instanceof LuaTable) {
            LuaTable t = (LuaTable) val;
            Object key = stack.pop();
            Object nextKey = t.nextKey(key);
            if (nextKey != null) {
                stack.push(nextKey);
                stack.push(t.get(nextKey));
                return true;
            }
            return false;
        }
        throw new RuntimeException("table expected!");
    }

    @Override
    public int error() {
        Object err = stack.pop();
        throw new RuntimeException(err.toString()); // TODO
    }

    @Override
    public boolean stringToNumber(String s) {
        Long i = LuaNumber.parseInteger(s);
        if (i != null) {
            pushInteger(i);
            return true;
        }
        Double f = LuaNumber.parseFloat(s);
        if (f != null) {
            pushNumber(f);
            return true;
        }
        return false;
    }

    /* LuaVM */

    @Override
    public void addPC(int n) {
        stack.pc += n;
    }

    @Override
    public int fetch() {
        return stack.closure.proto.getCode()[stack.pc++];
    }

    @Override
    public void getConst(int idx) {
        stack.push(stack.closure.proto.getConstants()[idx]);
    }

    @Override
    public void getRK(int rk) {
        if (rk > 0xFF) { // constant
            getConst(rk & 0xFF);
        } else { // register
            pushValue(rk + 1);
        }
    }

    @Override
    public int registerCount() {
        return stack.closure.proto.getMaxStackSize();
    }

    @Override
    public void loadVararg(int n) {
        List<Object> varargs = stack.varargs != null
                ? stack.varargs : Collections.emptyList();
        if (n < 0) {
            n = varargs.size();
        }

        //stack.check(n)
        stack.pushN(varargs, n);
    }

    @Override
    public void loadProto(int idx) {
        Prototype proto = stack.closure.proto.getProtos()[idx];
        Closure closure = new Closure(proto);
        stack.push(closure);

        for (int i = 0; i < proto.getUpvalues().length; i++) {
            Upvalue uvInfo = proto.getUpvalues()[i];
            int uvIdx = uvInfo.getIdx();
            if (uvInfo.getInstack() == 1) {
                if (stack.openuvs == null) {
                    stack.openuvs = new HashMap<>();
                }
                if (stack.openuvs.containsKey(uvIdx)) {
                    closure.upvals[i] = stack.openuvs.get(uvIdx);
                } else {
                    closure.upvals[i] = new UpvalueHolder(stack, uvIdx);
                    stack.openuvs.put(uvIdx, closure.upvals[i]);
                }
            } else {
                closure.upvals[i] = stack.closure.upvals[uvIdx];
            }
        }
    }

    public void closeUpvalues(int a) {
        if (stack.openuvs != null) {
            for (Iterator<UpvalueHolder> it = stack.openuvs.values().iterator(); it.hasNext(); ) {
                UpvalueHolder uv = it.next();
                if (uv.index >= a - 1) {
                    uv.migrate();
                    it.remove();
                }
            }
        }
    }

    /* aux lib */

    @Override
    public int error2(String fmt, Object... a) {
        pushFString(fmt, a); // todo
        return error();
    }

    @Override
    public int argError(int arg, String extraMsg) {
        return error2("bad argument #%d (%s)", arg, extraMsg); // todo
    }

    @Override
    public void checkStack2(int sz, String msg) {
        if (!checkStack(sz)) {
            if (msg != "") {
                error2("stack overflow (%s)", msg);
            } else {
                error2("stack overflow");
            }
        }
    }

    @Override
    public void argCheck(boolean cond, int arg, String extraMsg) {
        if (!cond) {
            argError(arg, extraMsg);
        }
    }

    @Override
    public void checkAny(int arg) {
        if (type(arg) == LUA_TNONE) {
            argError(arg, "value expected");
        }
    }

    @Override
    public void checkType(int arg, LuaType t) {
        if (type(arg) != t) {
            tagError(arg, t);
        }
    }

    @Override
    public long checkInteger(int arg) {
        Long i = toIntegerX(arg);
        if (i == null) {
            intError(arg);
        }
        return i;
    }

    @Override
    public double checkNumber(int arg) {
        Double f = toNumberX(arg);
        if (f == null) {
            tagError(arg, LUA_TNUMBER);
        }
        return f;
    }

    @Override
    public String checkString(int arg) {
        String s = toString(arg);
        if (s == null) {
            tagError(arg, LUA_TSTRING);
        }
        return s;
    }

    @Override
    public long optInteger(int arg, long dft) {
        return isNoneOrNil(arg) ? dft : checkInteger(arg);
    }

    @Override
    public double optNumber(int arg, double dft) {
        return isNoneOrNil(arg) ? dft : checkNumber(arg);
    }

    @Override
    public String optString(int arg, String dft) {
        return isNoneOrNil(arg) ? dft : checkString(arg);
    }

    @Override
    public boolean doFile(String filename) {
        return loadFile(filename) == LUA_OK &&
                pCall(0, LUA_MULTRET, 0) == LUA_OK;
    }

    @Override
    public boolean doString(String str) {
        return loadString(str) == LUA_OK &&
                pCall(0, LUA_MULTRET, 0) == LUA_OK;
    }

    @Override
    public ThreadStatus loadFile(String filename) {
        return loadFileX(filename, "bt");
    }

    @Override
    public ThreadStatus loadFileX(String filename, String mode) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(filename));
            return load(data, "@" + filename, mode);
        } catch (IOException e) {
            return LUA_ERRFILE;
        }
    }

    @Override
    public ThreadStatus loadString(String s) {
        return load(s.getBytes(), s, "bt");
    }

    @Override
    public String typeName2(int idx) {
        return typeName(type(idx));
    }

    @Override
    public long len2(int idx) {
        len(idx);
        Long i = toIntegerX(-1);
        if (i == null) {
            error2("object length is not an integer");
        }
        pop(1);
        return i;
    }

    @Override
    public String toString2(int idx) {
        if (callMeta(idx, "__tostring")) { /* metafield? */
            if (!isString(-1)) {
                error2("'__tostring' must return a string");
            }
        } else {
            switch (type(idx)) {
                case LUA_TNUMBER:
                    if (isInteger(idx)) {
                        pushString(String.format("%d", toInteger(idx))); // todo
                    } else {
                        pushString(String.format("%g", toNumber(idx))); // todo
                    }
                    break;
                case LUA_TSTRING:
                    pushValue(idx);
                    break;
                case LUA_TBOOLEAN:
                    pushString(toBoolean(idx) ? "true" : "false");
                    break;
                case LUA_TNIL:
                    pushString("nil");
                    break;
                default:
                    LuaType tt = getMetafield(idx, "__name"); /* try name */
                    String kind = tt == LUA_TSTRING ? checkString(-1) : typeName2(idx);
                    pushString(String.format("%s: %s", kind, Objects.hashCode(idx)));
                    if (tt != LUA_TNIL) {
                        remove(-2); /* remove '__name' */
                    }
                    break;
            }
        }
        return checkString(-1);
    }

    @Override
    public boolean getSubTable(int idx, String fname) {
        if (getField(idx, fname) == LUA_TTABLE) {
            return true; /* table already there */
        }
        pop(1); /* remove previous result */
        idx = stack.absIndex(idx);
        newTable();
        pushValue(-1);        /* copy to be left at top */
        setField(idx, fname); /* assign new table to field */
        return false;              /* false, because did not find table there */
    }

    @Override
    public LuaType getMetafield(int obj, String event) {
        if (!getMetatable(obj)) { /* no metatable? */
            return LUA_TNIL;
        }

        pushString(event);
        LuaType tt = rawGet(-2);
        if (tt == LUA_TNIL) { /* is metafield nil? */
            pop(2); /* remove metatable and metafield */
        } else {
            remove(-2); /* remove only metatable */
        }
        return tt; /* return metafield type */
    }

    @Override
    public boolean callMeta(int obj, String event) {
        obj = absIndex(obj);
        if (getMetafield(obj, event) == LUA_TNIL) { /* no metafield? */
            return false;
        }

        pushValue(obj);
        call(1, 1);
        return true;
    }

    @Override
    public void openLibs() {
        Map<String, JavaFunction> libs = new HashMap<>();
        libs.put("_G", BasicLib::openBaseLib);

        libs.forEach((name, fun) -> {
            requireF(name, fun, true);
            pop(1);
        });
    }

    @Override
    public void requireF(String modname, JavaFunction openf, boolean glb) {
        getSubTable(LUA_REGISTRYINDEX, "_LOADED");
        getField(-1, modname); /* LOADED[modname] */
        if (!toBoolean(-1)) {   /* package not already loaded? */
            pop(1); /* remove field */
            pushJavaFunction(openf);
            pushString(modname);   /* argument to open function */
            call(1, 1);            /* call 'openf' to open module */
            pushValue(-1);         /* make copy of module (call result) */
            setField(-3, modname); /* _LOADED[modname] = module */
        }
        remove(-2); /* remove _LOADED table */
        if (glb) {
            pushValue(-1);      /* copy of module */
            setGlobal(modname); /* _G[modname] = module */
        }
    }

    @Override
    public void newLib(Map<String, JavaFunction> l) {
        newLibTable(l);
        setFuncs(l, 0);
    }

    @Override
    public void newLibTable(Map<String, JavaFunction> l) {
        createTable(0, l.size());
    }

    @Override
    public void setFuncs(Map<String, JavaFunction> l, int nup) {
        checkStack2(nup, "too many upvalues");
        l.forEach((name, fun) -> { /* fill the table with given functions */
            for (int i = 0; i < nup; i++) { /* copy upvalues to the top */
                pushValue(-nup);
            }
            // r[-(nup+2)][name]=fun
            pushJavaClosure(fun, nup); /* closure with those upvalues */
            setField(-(nup + 2), name);
        });
        pop(nup); /* remove upvalues */
    }

    private void intError(int arg) {
        if (isNumber(arg)) {
            argError(arg, "number has no integer representation");
        } else {
            tagError(arg, LUA_TNUMBER);
        }
    }

    private void tagError(int arg, LuaType tag) {
        typeError(arg, typeName(tag));
    }

    private void typeError(int arg, String tname) {
        String typeArg; /* name for the type of the actual argument */
        if (getMetafield(arg, "__name") == LUA_TSTRING) {
            typeArg = toString(-1); /* use the given type name */
        } else if (type(arg) == LUA_TLIGHTUSERDATA) {
            typeArg = "light userdata"; /* special name for messages */
        } else {
            typeArg = typeName2(arg); /* standard name */
        }
        String msg = tname + " expected, got " + typeArg;
        pushString(msg);
        argError(arg, msg);
    }

}
