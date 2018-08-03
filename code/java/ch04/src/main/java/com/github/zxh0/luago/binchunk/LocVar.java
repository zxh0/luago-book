package com.github.zxh0.luago.binchunk;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@Setter
public class LocVar {

    private String varName;
    private int startPC;
    private int endPC;

    void read(ByteBuffer buf) {
        varName = BinaryChunk.getLuaString(buf);
        startPC = buf.getInt();
        endPC = buf.getInt();
    }

}
