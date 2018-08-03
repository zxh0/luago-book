package com.github.zxh0.luago.state;

import com.github.zxh0.luago.api.JavaFunction;
import com.github.zxh0.luago.binchunk.Prototype;
import lombok.Getter;

@Getter
class Closure {

    final Prototype proto;
    final JavaFunction javaFunc;

    // Lua Closure
    Closure(Prototype proto) {
        this.proto = proto;
        this.javaFunc = null;
    }

    Closure(JavaFunction javaFunc) {
        this.proto = null;
        this.javaFunc = javaFunc;
    }

}
