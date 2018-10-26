package com.github.zxh0.luago.binchunk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BinaryChunk {

    private static final byte[] LUA_SIGNATURE    = {0x1b, 'L', 'u', 'a'};
    private static final int    LUAC_VERSION     = 0x53;
    private static final int    LUAC_FORMAT      = 0;
    private static final byte[] LUAC_DATA        = {0x19, (byte) 0x93, '\r', '\n', 0x1a, '\n'};
    private static final int    CINT_SIZE        = 4;
    private static final int    CSIZET_SIZE      = 8;
    private static final int    INSTRUCTION_SIZE = 4;
    private static final int    LUA_INTEGER_SIZE = 8;
    private static final int    LUA_NUMBER_SIZE  = 8;
    private static final int    LUAC_INT         = 0x5678;
    private static final double LUAC_NUM         = 370.5;

    public static boolean isBinaryChunk(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (data[i] != LUA_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }

    public static Prototype undump(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);
        checkHead(buf);
        buf.get(); // size_upvalues
        Prototype mainFunc = new Prototype();
        mainFunc.read(buf, "");
        return mainFunc;
    }

    private static void checkHead(ByteBuffer buf) {
        if (!Arrays.equals(LUA_SIGNATURE, getBytes(buf, 4))) {
            throw new RuntimeException("not a precompiled chunk!");
        }
        if (buf.get() != LUAC_VERSION) {
            throw new RuntimeException("version mismatch!");
        }
        if (buf.get() != LUAC_FORMAT) {
            throw new RuntimeException("format mismatch!");
        }
        if (!Arrays.equals(LUAC_DATA, getBytes(buf, 6))) {
            throw new RuntimeException("corrupted!");
        }
        if (buf.get() != CINT_SIZE) {
            throw new RuntimeException("int size mismatch!");
        }
        if (buf.get() != CSIZET_SIZE) {
            throw new RuntimeException("size_t size mismatch!");
        }
        if (buf.get() != INSTRUCTION_SIZE) {
            throw new RuntimeException("instruction size mismatch!");
        }
        if (buf.get() != LUA_INTEGER_SIZE) {
            throw new RuntimeException("lua_Integer size mismatch!");
        }
        if (buf.get() != LUA_NUMBER_SIZE) {
            throw new RuntimeException("lua_Number size mismatch!");
        }
        if (buf.getLong() != LUAC_INT) {
            throw new RuntimeException("endianness mismatch!");
        }
        if (buf.getDouble() != LUAC_NUM) {
            throw new RuntimeException("float format mismatch!");
        }
    }

    static String getLuaString(ByteBuffer buf) {
        int size = buf.get() & 0xFF;
        if (size == 0) {
            return "";
        }
        if (size == 0xFF) {
            size = (int) buf.getLong(); // size_t
        }

        byte[] a = getBytes(buf, size - 1);
        return new String(a); // todo
    }

    private static byte[] getBytes(ByteBuffer buf, int n) {
        byte[] a = new byte[n];
        buf.get(a);
        return a;
    }

}
