package com.github.zxh0.luago.vm;

public class Instruction {

    public static final int MAXARG_Bx = (1 << 18) - 1;   // 262143
    public static final int MAXARG_sBx = MAXARG_Bx >> 1; // 131071

    public static OpCode getOpCode(int i) {
        return OpCode.values()[i & 0x3F];
    }

    public static int getA(int i) {
        return (i >> 6) & 0xFF;
    }

    public static int getC(int i) {
        return (i >> 14) & 0x1FF;
    }

    public static int getB(int i) {
        return (i >> 23) & 0x1FF;
    }

    public static int getBx(int i) {
        return i >>> 14;
    }

    public static int getSBx(int i) {
        return getBx(i) - MAXARG_sBx;
    }

    public static int getAx(int i) {
        return i >>> 6;
    }

}
