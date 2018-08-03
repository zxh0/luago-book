package com.github.zxh0.luago.compiler.codegen;

import com.github.zxh0.luago.binchunk.LocVar;
import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.binchunk.Upvalue;

import java.util.List;

class Fi2Proto {

    static Prototype toProto(FuncInfo fi) {
        Prototype proto = new Prototype();
        proto.setLineDefined(fi.line);
        proto.setLastLineDefined(fi.lastLine);
        proto.setNumParams((byte) fi.numParams);
        proto.setMaxStackSize((byte) fi.maxRegs);
        proto.setCode(fi.insts.stream().mapToInt(Integer::intValue).toArray());
        proto.setConstants(getConstants(fi));
        proto.setUpvalues(getUpvalues(fi));
        proto.setProtos(toProtos(fi.subFuncs));
        proto.setLineInfo(fi.lineNums.stream().mapToInt(Integer::intValue).toArray());
        proto.setLocVars(getLocVars(fi));
        proto.setUpvalueNames(getUpvalueNames(fi));

        if (fi.line == 0) {
            proto.setLastLineDefined(0);
        }
        if (proto.getMaxStackSize() < 2) {
            proto.setMaxStackSize((byte) 2); // todo
        }
        if (fi.isVararg) {
            proto.setIsVararg((byte) 1); // todo
        }

        return proto;
    }

    private static Prototype[] toProtos(List<FuncInfo> fis) {
        return fis.stream()
                .map(Fi2Proto::toProto)
                .toArray(Prototype[]::new);
    }

    private static Object[] getConstants(FuncInfo fi) {
        Object[] consts = new Object[fi.constants.size()];
        fi.constants.forEach((c, idx) -> consts[idx] = c);
        return consts;
    }

    private static LocVar[] getLocVars(FuncInfo fi) {
        return fi.locVars.stream()
                .map(locVarInfo -> {
                    LocVar var = new LocVar();
                    var.setVarName(locVarInfo.name);
                    var.setStartPC(locVarInfo.startPC);
                    var.setEndPC(locVarInfo.endPC);
                    return var;
                })
                .toArray(LocVar[]::new);
    }

    private static Upvalue[] getUpvalues(FuncInfo fi) {
        Upvalue[] upvals = new Upvalue[fi.upvalues.size()];

        for (FuncInfo.UpvalInfo uvInfo : fi.upvalues.values()) {
            Upvalue upval = new Upvalue();
            upvals[uvInfo.index] = upval;
            if (uvInfo.locVarSlot >= 0) { // instack
                upval.setInstack((byte) 1);
                upval.setIdx((byte) uvInfo.locVarSlot);
            } else {
                upval.setInstack((byte) 0);
                upval.setIdx((byte) uvInfo.upvalIndex);
            }
        }

        return upvals;
    }

    private static String[] getUpvalueNames(FuncInfo fi) {
        String[] names = new String[fi.upvalues.size()];
        fi.upvalues.forEach((name, uvInfo) -> names[uvInfo.index] = name);
        return names;
    }

}
