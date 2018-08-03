package com.github.zxh0.luago.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LuaStack {

    /* virtual stack */
    private final ArrayList<Object> slots = new ArrayList<>();
    /* call info */
    Closure closure;
    List<Object> varargs;
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
        return idx >= 0 ? idx : idx + slots.size() + 1;
    }

    boolean isValid(int idx) {
        int absIdx = absIndex(idx);
        return absIdx > 0 && absIdx <= slots.size();
    }

    Object get(int idx) {
        int absIdx = absIndex(idx);
        if (absIdx > 0 && absIdx <= slots.size()) {
            return slots.get(absIdx - 1);
        } else {
            return null;
        }
    }

    void set(int idx, Object val) {
        int absIdx = absIndex(idx);
        slots.set(absIdx - 1, val);
    }

    void reverse(int from, int to) {
        Collections.reverse(slots.subList(from, to + 1));
    }

}
