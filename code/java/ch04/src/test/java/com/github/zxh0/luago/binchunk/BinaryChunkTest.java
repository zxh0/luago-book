package com.github.zxh0.luago.binchunk;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class BinaryChunkTest {

    @Test
    public void undump() throws Exception {
        byte[] hwData = Files.readAllBytes(Paths.get(
                getClass().getResource("/hello_world.luac").toURI()));
        Prototype proto = BinaryChunk.undump(hwData);

        assertEquals("@hello_world.lua", proto.getSource());
        assertEquals(0, proto.getLineDefined());
        assertEquals(0, proto.getLastLineDefined());
        assertEquals(0, proto.getNumParams());
        assertEquals(1, proto.getIsVararg());
        assertEquals(2, proto.getMaxStackSize());
        assertEquals(4, proto.getCode().length);
        assertEquals(2, proto.getConstants().length);
        assertEquals(1, proto.getUpvalues().length);
        assertEquals(0, proto.getProtos().length);
        assertEquals(4, proto.getLineInfo().length);
        assertEquals(0, proto.getLocVars().length);
        assertEquals(1, proto.getUpvalueNames().length);

        assertEquals("print", proto.getConstants()[0]);
        assertEquals("Hello, World!", proto.getConstants()[1]);
        assertEquals("_ENV", proto.getUpvalueNames()[0]);
    }

}
