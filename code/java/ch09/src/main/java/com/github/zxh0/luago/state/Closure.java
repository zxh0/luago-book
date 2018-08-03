package com.github.zxh0.luago.state;

import com.github.zxh0.luago.binchunk.Prototype;
import lombok.Getter;

@Getter
class Closure {

    final Prototype proto;

    Closure(Prototype proto) {
        this.proto = proto;
    }

}
