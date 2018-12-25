package com.github.zxh0.luago.state;

import com.github.zxh0.luago.api.*;
import com.github.zxh0.luago.binchunk.BinaryChunk;
import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.binchunk.Upvalue;
import com.github.zxh0.luago.vm.Instruction;
import com.github.zxh0.luago.vm.OpCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.github.zxh0.luago.api.ArithOp.*;
import static com.github.zxh0.luago.api.LuaType.*;
import static com.github.zxh0.luago.api.ThreadStatus.LUA_OK;

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
        Object result = Arithmetic.arith(a, b, op);
        if (result != null) {
            stack.push(result);
        } else {
            throw new RuntimeException("arithmetic error!");
        }
    }

    @Override
    public boolean compare(int idx1, int idx2, CmpOp op) {
        if (!stack.isValid(idx1) || !stack.isValid(idx2)) {
            return false;
        }

        Object a = stack.get(idx1);
        Object b = stack.get(idx2);
        switch (op) {
            case LUA_OPEQ: return Comparison.eq(a, b);
            case LUA_OPLT: return Comparison.lt(a, b);
            case LUA_OPLE: return Comparison.le(a, b);
            default: throw new RuntimeException("invalid compare op!");
        }
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
        return getTable(t, k);
    }

    @Override
    public LuaType getField(int idx, String k) {
        Object t = stack.get(idx);
        return getTable(t, k);
    }

    @Override
    public LuaType getI(int idx, long i) {
        Object t = stack.get(idx);
        return getTable(t, i);
    }

    @Override
    public LuaType getGlobal(String name) {
        Object t = registry.get(LUA_RIDX_GLOBALS);
        return getTable(t, name);
    }

    private LuaType getTable(Object t, Object k) {
        if (t instanceof LuaTable) {
            Object v = ((LuaTable) t).get(k);
            stack.push(v);
            return LuaValue.typeOf(v);
        }
        throw new RuntimeException("not a table!"); // todo
    }

    /* set functions (stack -> Lua) */

    @Override
    public void setTable(int idx) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        Object k = stack.pop();
        setTable(t, k, v);
    }

    @Override
    public void setField(int idx, String k) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, k, v);
    }

    @Override
    public void setI(int idx, long i) {
        Object t = stack.get(idx);
        Object v = stack.pop();
        setTable(t, i, v);
    }

    @Override
    public void setGlobal(String name) {
        Object t = registry.get(LUA_RIDX_GLOBALS);
        Object v = stack.pop();
        setTable(t, name, v);
    }

    @Override
    public void register(String name, JavaFunction f) {
        pushJavaFunction(f);
        setGlobal(name);
    }

    private void setTable(Object t, Object k, Object v) {
        if (t instanceof LuaTable) {
            ((LuaTable) t).put(k, v);
            return;
        }
        throw new RuntimeException("not a table!");
    }

    /* 'load' and 'call' functions */

    @Override
    public ThreadStatus load(byte[] chunk, String chunkName, String mode) {
        Prototype proto = BinaryChunk.undump(chunk); // todo
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
        if (val instanceof Closure) {
            Closure c = (Closure) val;
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

    /* miscellaneous functions */

    @Override
    public void len(int idx) {
        Object val = stack.get(idx);
        if (val instanceof String) {
            pushInteger(((String) val).length());
        } else if (val instanceof LuaTable) {
            pushInteger(((LuaTable) val).length());
        } else {
            throw new RuntimeException("length error!");
        }
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

                throw new RuntimeException("concatenation error!");
            }
        }
        // n == 1, do nothing
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

}
