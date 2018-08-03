package com.github.zxh0.luago.state;

import java.util.ArrayList;
import java.util.Collections;

class LuaStack {

    private final ArrayList<Object> slots = new ArrayList<>();

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
