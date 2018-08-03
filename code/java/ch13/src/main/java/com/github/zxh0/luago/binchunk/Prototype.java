package com.github.zxh0.luago.binchunk;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

// function prototype
@Getter
@Setter
public class Prototype {

    private static final int TAG_NIL       = 0x00;
    private static final int TAG_BOOLEAN   = 0x01;
    private static final int TAG_NUMBER    = 0x03;
    private static final int TAG_INTEGER   = 0x13;
    private static final int TAG_SHORT_STR = 0x04;
    private static final int TAG_LONG_STR  = 0x14;


    private String source; // debug
    private int lineDefined;
    private int lastLineDefined;
    private byte numParams;
    private byte isVararg;
    private byte maxStackSize;
    private int[] code;
    private Object[] constants;
    private Upvalue[] upvalues;
    private Prototype[] protos;
    private int[] lineInfo;        // debug
    private LocVar[] locVars;      // debug
    private String[] upvalueNames; // debug

    void read(ByteBuffer buf, String parentSource) {
        source = BinaryChunk.getLuaString(buf);
        if (source.isEmpty()) {
            source = parentSource;
        }
        lineDefined = buf.getInt();
        lastLineDefined = buf.getInt();
        numParams = buf.get();
        isVararg = buf.get();
        maxStackSize = buf.get();
        readCode(buf);
        readConstants(buf);
        readUpvalues(buf);
        readProtos(buf, source);
        readLineInfo(buf);
        readLocVars(buf);
        readUpvalueNames(buf);
    }

    private void readCode(ByteBuffer buf) {
        code = new int[buf.getInt()];
        for (int i = 0; i < code.length; i++) {
            code[i] = buf.getInt();
        }
    }

    private void readConstants(ByteBuffer buf) {
        constants = new Object[buf.getInt()];
        for (int i = 0; i < constants.length; i++) {
            constants[i] = readConstant(buf);
        }
    }

    private Object readConstant(ByteBuffer buf) {
        switch (buf.get()) {
            case TAG_NIL: return null;
            case TAG_BOOLEAN: return buf.get() != 0;
            case TAG_INTEGER: return buf.getLong();
            case TAG_NUMBER: return buf.getDouble();
            case TAG_SHORT_STR: return BinaryChunk.getLuaString(buf);
            case TAG_LONG_STR: return BinaryChunk.getLuaString(buf);
            default: throw new RuntimeException("corrupted!"); // todo
        }
    }

    private void readUpvalues(ByteBuffer buf) {
        upvalues = new Upvalue[buf.getInt()];
        for (int i = 0; i < upvalues.length; i++) {
            upvalues[i] = new Upvalue();
            upvalues[i].read(buf);
        }
    }

    private void readProtos(ByteBuffer buf, String parentSource) {
        protos = new Prototype[buf.getInt()];
        for (int i = 0; i < protos.length; i++) {
            protos[i] = new Prototype();
            protos[i].read(buf, parentSource);
        }
    }

    private void readLineInfo(ByteBuffer buf) {
        lineInfo = new int[buf.getInt()];
        for (int i = 0; i < lineInfo.length; i++) {
            lineInfo[i] = buf.getInt();
        }
    }

    private void readLocVars(ByteBuffer buf) {
        locVars = new LocVar[buf.getInt()];
        for (int i = 0; i < locVars.length; i++) {
            locVars[i] = new LocVar();
            locVars[i].read(buf);
        }
    }

    private void readUpvalueNames(ByteBuffer buf) {
        upvalueNames = new String[buf.getInt()];
        for (int i = 0; i < upvalueNames.length; i++) {
            upvalueNames[i] = BinaryChunk.getLuaString(buf);
        }
    }

}
