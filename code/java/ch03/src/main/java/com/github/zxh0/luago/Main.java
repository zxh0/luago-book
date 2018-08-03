package com.github.zxh0.luago;

import com.github.zxh0.luago.binchunk.BinaryChunk;
import com.github.zxh0.luago.binchunk.LocVar;
import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.binchunk.Upvalue;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            byte[] data = Files.readAllBytes(Paths.get(args[0]));
            Prototype proto = BinaryChunk.undump(data);
            list(proto);
        }
    }

    private static void list(Prototype f) {
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
            System.out.printf("\t%d\t[%s]\t0x%08X\n", i+1, line, code[i]);
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
