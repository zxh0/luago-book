package com.github.zxh0.luago;

import com.github.zxh0.luago.binchunk.LocVar;
import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.binchunk.Upvalue;
import com.github.zxh0.luago.vm.OpCode;

import static com.github.zxh0.luago.vm.Instruction.*;
import static com.github.zxh0.luago.vm.OpArgMask.*;

public class ProtoPrinter {

    public static void list(Prototype f) {
        printHeader(f);
        printCode(f);
        printDetail(f);
        for (Prototype p : f.getProtos()) {
            list(p);
        }
    }

    private static void printHeader(Prototype f) {
        String funcType = f.getLineDefined() > 0 ? "function" : "main";
        String varargFlag = f.getIsVararg() > 0 ? "+" : "";

        System.out.printf("\n%s <%s:%d,%d> (%d instructions)\n",
                funcType, f.getSource(), f.getLineDefined(), f.getLastLineDefined(),
                f.getCode().length);

        System.out.printf("%d%s params, %d slots, %d upvalues, ",
                f.getNumParams(), varargFlag, f.getMaxStackSize(), f.getUpvalues().length);

        System.out.printf("%d locals, %d constants, %d functions\n",
                f.getLocVars().length, f.getConstants().length, f.getProtos().length);
    }

    private static void printCode(Prototype f) {
        int[] code = f.getCode();
        int[] lineInfo = f.getLineInfo();
        for (int i = 0; i < code.length; i++) {
            String line = lineInfo.length > 0 ? String.valueOf(lineInfo[i]) : "-";
            System.out.printf("\t%d\t[%s]\t%-8s \t", i + 1, line, getOpCode(code[i]));
            printOperands(code[i]);
            System.out.println();
        }
    }

    private static void printOperands(int i) {
        OpCode opCode = getOpCode(i);
        int a = getA(i);
        switch (opCode.getOpMode()) {
            case iABC:
                System.out.printf("%d", a);
                if (opCode.getArgBMode() != OpArgN) {
                    int b = getB(i);
                    System.out.printf(" %d", b > 0xFF ? -1 - (b & 0xFF) : b);
                }
                if (opCode.getArgCMode() != OpArgN) {
                    int c = getC(i);
                    System.out.printf(" %d", c > 0xFF ? -1 - (c & 0xFF) : c);
                }
                break;
            case iABx:
                System.out.printf("%d", a);
                int bx = getBx(i);
                if (opCode.getArgBMode() == OpArgK) {
                    System.out.printf(" %d", -1 - bx);
                } else if (opCode.getArgBMode() == OpArgU) {
                    System.out.printf(" %d", bx);
                }
                break;
            case iAsBx:
                int sbx = getSBx(i);
                System.out.printf("%d %d", a, sbx);
                break;
            case iAx:
                int ax = getAx(i);
                System.out.printf("%d", -1 - ax);
                break;
        }
    }

    private static void printDetail(Prototype f) {
        System.out.printf("constants (%d):\n", f.getConstants().length);
        int i = 1;
        for (Object k : f.getConstants()) {
            System.out.printf("\t%d\t%s\n", i++, constantToString(k));
        }

        i = 0;
        System.out.printf("locals (%d):\n", f.getLocVars().length);
        for (LocVar locVar : f.getLocVars()) {
            System.out.printf("\t%d\t%s\t%d\t%d\n", i++,
                    locVar.getVarName(), locVar.getStartPC() + 1, locVar.getEndPC() + 1);
        }

        i = 0;
        System.out.printf("upvalues (%d):\n", f.getUpvalues().length);
        for (Upvalue upval : f.getUpvalues()) {
            String name = f.getUpvalueNames().length > 0 ? f.getUpvalueNames()[i] : "-";
            System.out.printf("\t%d\t%s\t%d\t%d\n", i++,
                    name, upval.getInstack(), upval.getIdx());
        }
    }

    private static String constantToString(Object k) {
        if (k == null) {
            return "nil";
        } else if (k instanceof String) {
            return "\"" + k + "\"";
        } else {
            return k.toString();
        }
    }

}
