package com.github.zxh0.luago.state;

import com.github.zxh0.luago.api.JavaFunction;
import com.github.zxh0.luago.binchunk.Prototype;
import lombok.Getter;

@Getter
class Closure {

    final Prototype proto;
    final JavaFunction javaFunc;
    final UpvalueHolder[] upvals;

    // Lua Closure
    Closure(Prototype proto) {
        this.proto = proto;
        this.javaFunc = null;
        this.upvals = new UpvalueHolder[proto.getUpvalues().length];
    }

    Closure(JavaFunction javaFunc, int nUpvals) {
        this.proto = null;
        this.javaFunc = javaFunc;
        this.upvals = new UpvalueHolder[nUpvals];
    }

}
