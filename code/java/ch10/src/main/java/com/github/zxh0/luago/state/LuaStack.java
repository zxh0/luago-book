package com.github.zxh0.luago.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.zxh0.luago.api.LuaState.LUA_REGISTRYINDEX;

class LuaStack {

    /* virtual stack */
    private final ArrayList<Object> slots = new ArrayList<>();
    /* call info */
    LuaStateImpl state;
    Closure closure;
    List<Object> varargs;
    Map<Integer, UpvalueHolder> openuvs;
    int pc;
    /* linked list */
    LuaStack prev;

    int top() {
        return slots.size();
    }

    void push(Object val) {
        if (slots.size() > 10000) { // TODO
            throw new StackOverflowError();
        }
        slots.add(val);
    }

    Object pop() {
        return slots.remove(slots.size() - 1);
    }

    void pushN(List<Object> vals, int n) {
        int nVals = vals == null ? 0 : vals.size();
        if (n < 0) {
            n = nVals;
        }
        for (int i = 0; i < n; i++) {
            push(i < nVals ? vals.get(i) : null);
        }
    }

    List<Object> popN(int n) {
        List<Object> vals = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            vals.add(pop());
        }
        Collections.reverse(vals);
        return vals;
    }

    int absIndex(int idx) {
        return idx >= 0 || idx <= LUA_REGISTRYINDEX
                ? idx : idx + slots.size() + 1;
    }

    boolean isValid(int idx) {
        if (idx < LUA_REGISTRYINDEX) { /* upvalues */
            int uvIdx = LUA_REGISTRYINDEX - idx - 1;
            return closure != null && uvIdx < closure.upvals.length;
        }
        if (idx == LUA_REGISTRYINDEX) {
            return true;
        }
        int absIdx = absIndex(idx);
        return absIdx > 0 && absIdx <= slots.size();
    }

    Object get(int idx) {
        if (idx < LUA_REGISTRYINDEX) { /* upvalues */
            int uvIdx = LUA_REGISTRYINDEX - idx - 1;
            if (closure != null
                    && closure.upvals.length > uvIdx
                    && closure.upvals[uvIdx] != null) {
                return closure.upvals[uvIdx].get();
            } else {
                return null;
            }
        }
        if (idx == LUA_REGISTRYINDEX) {
            return state.registry;
        }
        int absIdx = absIndex(idx);
        if (absIdx > 0 && absIdx <= slots.size()) {
            return slots.get(absIdx - 1);
        } else {
            return null;
        }
    }

    void set(int idx, Object val) {
        if (idx < LUA_REGISTRYINDEX) { /* upvalues */
            int uvIdx = LUA_REGISTRYINDEX - idx - 1;
            if (closure != null
                    && closure.upvals.length > uvIdx
                    && closure.upvals[uvIdx] != null) {
                closure.upvals[uvIdx].set(val);
            }
            return;
        }
        if (idx == LUA_REGISTRYINDEX) {
            state.registry = (LuaTable) val;
            return;
        }
        int absIdx = absIndex(idx);
        slots.set(absIdx - 1, val);
    }

    void reverse(int from, int to) {
        Collections.reverse(slots.subList(from, to + 1));
    }

}
