package com.github.zxh0.luago.state;

class UpvalueHolder {

    final int index;
    private LuaStack stack;
    private Object value;

    UpvalueHolder(Object value) {
        this.value = value;
        this.index = 0;
    }

    UpvalueHolder(LuaStack stack, int index) {
        this.stack = stack;
        this.index = index;
    }

    Object get() {
        return stack != null ? stack.get(index + 1) : value;
    }

    void set(Object value) {
        if (stack != null) {
            stack.set(index + 1, value);
        } else {
            this.value = value;
        }
    }

    void migrate() {
        if (stack != null) {
            value = stack.get(index + 1);
            stack = null;
        }
    }

}
