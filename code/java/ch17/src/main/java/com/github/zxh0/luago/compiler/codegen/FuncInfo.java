package com.github.zxh0.luago.compiler.codegen;

import com.github.zxh0.luago.compiler.ast.exps.FuncDefExp;
import com.github.zxh0.luago.compiler.lexer.TokenKind;
import com.github.zxh0.luago.vm.FPB;
import com.github.zxh0.luago.vm.OpCode;

import java.util.*;

import static com.github.zxh0.luago.compiler.lexer.TokenKind.*;
import static com.github.zxh0.luago.vm.Instruction.MAXARG_sBx;

class FuncInfo {

    private static final Map<TokenKind, OpCode> arithAndBitwiseBinops = new HashMap<>();
    static {
        arithAndBitwiseBinops.put(TOKEN_OP_ADD,  OpCode.ADD);
        arithAndBitwiseBinops.put(TOKEN_OP_SUB,  OpCode.SUB);
        arithAndBitwiseBinops.put(TOKEN_OP_MUL,  OpCode.MUL);
        arithAndBitwiseBinops.put(TOKEN_OP_MOD,  OpCode.MOD);
        arithAndBitwiseBinops.put(TOKEN_OP_POW,  OpCode.POW);
        arithAndBitwiseBinops.put(TOKEN_OP_DIV,  OpCode.DIV);
        arithAndBitwiseBinops.put(TOKEN_OP_IDIV, OpCode.IDIV);
        arithAndBitwiseBinops.put(TOKEN_OP_BAND, OpCode.BAND);
        arithAndBitwiseBinops.put(TOKEN_OP_BOR,  OpCode.BOR);
        arithAndBitwiseBinops.put(TOKEN_OP_BXOR, OpCode.BXOR);
        arithAndBitwiseBinops.put(TOKEN_OP_SHL,  OpCode.SHL);
        arithAndBitwiseBinops.put(TOKEN_OP_SHR,  OpCode.SHR);
    }

    static class UpvalInfo {
        int locVarSlot;
        int upvalIndex;
        int index;
    }

    static class LocVarInfo {
        LocVarInfo prev;
        String name;
        int scopeLv;
        int slot;
        int startPC;
        int endPC;
        boolean captured;
    }

    private FuncInfo parent;
    List<FuncInfo> subFuncs = new ArrayList<>();
    int usedRegs;
    int maxRegs;
    private int scopeLv;
    List<LocVarInfo> locVars = new ArrayList<>();
    private Map<String, LocVarInfo> locNames = new HashMap<>();
    Map<String, UpvalInfo> upvalues = new HashMap<>();
    Map<Object, Integer> constants = new HashMap<>();
    private List<List<Integer>> breaks = new ArrayList<>();
    List<Integer> insts = new ArrayList<>();
    List<Integer> lineNums = new ArrayList<>();
    int line;
    int lastLine;
    int numParams;
    boolean isVararg;

    FuncInfo(FuncInfo parent, FuncDefExp fd) {
        this.parent = parent;
        line = fd.getLine();
        lastLine = fd.getLastLine();
        numParams = fd.getParList() != null ? fd.getParList().size() : 0;
        isVararg = fd.isIsVararg();
        breaks.add(null);
    }

    /* constants */

    int indexOfConstant(Object k) {
        Integer idx = constants.get(k);
        if (idx != null) {
            return idx;
        }

        idx = constants.size();
        constants.put(k, idx);
        return idx;
    }

    /* registers */

    int allocReg() {
        usedRegs++;
        if (usedRegs >= 255) {
            throw new RuntimeException("function or expression needs too many registers");
        }
        if (usedRegs > maxRegs) {
            maxRegs = usedRegs;
        }
        return usedRegs - 1;
    }

    void freeReg() {
        if (usedRegs <= 0) {
            throw new RuntimeException("usedRegs <= 0 !");
        }
        usedRegs--;
    }

    int allocRegs(int n) {
        if (n <= 0) {
            throw new RuntimeException("n <= 0 !");
        }
        for (int i = 0; i < n; i++) {
            allocReg();
        }
        return usedRegs - n;
    }

    void freeRegs(int n) {
        if (n < 0) {
            throw new RuntimeException("n < 0 !");
        }
        for (int i = 0; i < n; i++) {
            freeReg();
        }
    }

    /* lexical scope */

    void enterScope(boolean breakable) {
        scopeLv++;
        if (breakable) {
            breaks.add(new ArrayList<>());
        } else {
            breaks.add(null);
        }
    }

    void exitScope(int endPC) {
        List<Integer> pendingBreakJmps = breaks.remove(breaks.size() - 1);

        if (pendingBreakJmps != null) {
            int a = getJmpArgA();
            for (int pc : pendingBreakJmps) {
                int sBx = pc() - pc;
                int i = (sBx+MAXARG_sBx)<<14 | a<<6 | OpCode.JMP.ordinal();
                insts.set(pc, i);
            }
        }

        scopeLv--;
        for (LocVarInfo locVar : new ArrayList<>(locNames.values())) {
            if (locVar.scopeLv > scopeLv) { // out of scope
                locVar.endPC = endPC;
                removeLocVar(locVar);
            }
        }
    }

    private void removeLocVar(LocVarInfo locVar) {
        freeReg();
        if (locVar.prev == null) {
            locNames.remove(locVar.name);
        } else if (locVar.prev.scopeLv == locVar.scopeLv) {
            removeLocVar(locVar.prev);
        } else {
            locNames.put(locVar.name, locVar.prev);
        }
    }

    int addLocVar(String name, int startPC) {
        LocVarInfo newVar = new LocVarInfo();
        newVar.name = name;
        newVar.prev = locNames.get(name);
        newVar.scopeLv = scopeLv;
        newVar.slot = allocReg();
        newVar.startPC = startPC;
        newVar.endPC = 0;

        locVars.add(newVar);
        locNames.put(name, newVar);

        return newVar.slot;
    }

    int slotOfLocVar(String name) {
        return locNames.containsKey(name)
                ? locNames.get(name).slot
                : -1;
    }

    void addBreakJmp(int pc) {
        for (int i = scopeLv; i >= 0; i--) {
            if (breaks.get(i) != null) { // breakable
                breaks.get(i).add(pc);
                return;
            }
        }

        throw new RuntimeException("<break> at line ? not inside a loop!");
    }

    /* upvalues */

    int indexOfUpval(String name) {
        if (upvalues.containsKey(name)) {
            return upvalues.get(name).index;
        }
        if (parent != null) {
            if (parent.locNames.containsKey(name)) {
                LocVarInfo locVar = parent.locNames.get(name);
                int idx = upvalues.size();
                UpvalInfo upval = new UpvalInfo();
                upval.locVarSlot = locVar.slot;
                upval.upvalIndex = -1;
                upval.index = idx;
                upvalues.put(name, upval);
                locVar.captured = true;
                return idx;
            }
            int uvIdx = parent.indexOfUpval(name);
            if (uvIdx >= 0) {
                int idx = upvalues.size();
                UpvalInfo upval = new UpvalInfo();
                upval.locVarSlot = -1;
                upval.upvalIndex = uvIdx;
                upval.index = idx;
                upvalues.put(name, upval);
                return idx;
            }
        }
        return -1;
    }

    void closeOpenUpvals(int line) {
        int a = getJmpArgA();
        if (a > 0) {
            emitJmp(line, a, 0);
        }
    }

    int getJmpArgA() {
        boolean hasCapturedLocVars = false;
        int minSlotOfLocVars = maxRegs;
        for (LocVarInfo locVar : locNames.values()) {
            if (locVar.scopeLv == scopeLv) {
                for (LocVarInfo v = locVar; v != null && v.scopeLv == scopeLv; v = v.prev) {
                    if (v.captured) {
                        hasCapturedLocVars = true;
                    }
                    if (v.slot < minSlotOfLocVars && v.name.charAt(0) != '(') {
                        minSlotOfLocVars = v.slot;
                    }
                }
            }
        }
        if (hasCapturedLocVars) {
            return minSlotOfLocVars + 1;
        } else {
            return 0;
        }
    }

    /* code */

    int pc() {
        return insts.size() - 1;
    }

    void fixSbx(int pc, int sBx) {
        int i = insts.get(pc);
        i = i << 18 >> 18;                  // clear sBx
        i = i | (sBx+MAXARG_sBx)<<14; // reset sBx
        insts.set(pc, i);
    }

    // todo: rename?
    void fixEndPC(String name, int delta) {
        for (int i = locVars.size() - 1; i >= 0; i--) {
            LocVarInfo locVar = locVars.get(i);
            if (locVar.name.equals(name)) {
                locVar.endPC += delta;
                return;
            }
        }
    }

    void emitABC(int line, OpCode opcode, int a, int b, int c) {
        int i = b<<23 | c<<14 | a<<6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    private void emitABx(int line, OpCode opcode, int a, int bx) {
        int i = bx<<14 | a<<6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    private void emitAsBx(int line, OpCode opcode, int a, int sBx) {
        int i = (sBx+MAXARG_sBx)<<14 | a<<6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    private void emitAx(int line, OpCode opcode, int ax) {
        int i = ax<<6 | opcode.ordinal();
        insts.add(i);
        lineNums.add(line);
    }

    // r[a] = r[b]
    void emitMove(int line, int a, int b) {
        emitABC(line, OpCode.MOVE, a, b, 0);
    }

    // r[a], r[a+1], ..., r[a+b] = nil
    void emitLoadNil(int line, int a, int n) {
        emitABC(line, OpCode.LOADNIL, a, n-1, 0);
    }

    // r[a] = (bool)b; if (c) pc++
    void emitLoadBool(int line, int a, int b, int c) {
        emitABC(line, OpCode.LOADBOOL, a, b, c);
    }

    // r[a] = kst[bx]
    void emitLoadK(int line, int a, Object k) {
        int idx = indexOfConstant(k);
        if (idx < (1 << 18)) {
            emitABx(line, OpCode.LOADK, a, idx);
        } else {
            emitABx(line, OpCode.LOADKX, a, 0);
            emitAx(line, OpCode.EXTRAARG, idx);
        }
    }

    // r[a], r[a+1], ..., r[a+b-2] = vararg
    void emitVararg(int line, int a, int n) {
        emitABC(line, OpCode.VARARG, a, n+1, 0);
    }

    // r[a] = emitClosure(proto[bx])
    void emitClosure(int line, int a, int bx) {
        emitABx(line, OpCode.CLOSURE, a, bx);
    }

    // r[a] = {}
    void emitNewTable(int line, int a, int nArr, int nRec) {
        emitABC(line, OpCode.NEWTABLE,
                a, FPB.int2fb(nArr), FPB.int2fb(nRec));
    }

    // r[a][(c-1)*FPF+i] = r[a+i], 1 <= i <= b
    void emitSetList(int line, int a, int b, int c) {
        emitABC(line, OpCode.SETLIST, a, b, c);
    }

    // r[a] = r[b][rk(c)]
    void emitGetTable(int line, int a, int b, int c) {
        emitABC(line, OpCode.GETTABLE, a, b, c);
    }

    // r[a][rk(b)] = rk(c)
    void emitSetTable(int line, int a, int b, int c) {
        emitABC(line, OpCode.SETTABLE, a, b, c);
    }

    // r[a] = upval[b]
    void emitGetUpval(int line, int a, int b) {
        emitABC(line, OpCode.GETUPVAL, a, b, 0);
    }

    // upval[b] = r[a]
    void emitSetUpval(int line, int a, int b) {
        emitABC(line, OpCode.SETUPVAL, a, b, 0);
    }

    // r[a] = upval[b][rk(c)]
    void emitGetTabUp(int line, int a, int b, int c) {
        emitABC(line, OpCode.GETTABUP, a, b, c);
    }

    // upval[a][rk(b)] = rk(c)
    void emitSetTabUp(int line, int a, int b, int c) {
        emitABC(line, OpCode.SETTABUP, a, b, c);
    }

    // r[a], ..., r[a+c-2] = r[a](r[a+1], ..., r[a+b-1])
    void emitCall(int line, int a, int nArgs, int nRet) {
        emitABC(line, OpCode.CALL, a, nArgs+1, nRet+1);
    }

    // return r[a](r[a+1], ... ,r[a+b-1])
    void emitTailCall(int line, int a, int nArgs) {
        emitABC(line, OpCode.TAILCALL, a, nArgs+1, 0);
    }

    // return r[a], ... ,r[a+b-2]
    void emitReturn(int line, int a, int n) {
        emitABC(line, OpCode.RETURN, a, n+1, 0);
    }

    // r[a+1] = r[b]; r[a] = r[b][rk(c)]
    void emitSelf(int line, int a, int b, int c) {
        emitABC(line, OpCode.SELF, a, b, c);
    }

    // pc+=sBx; if (a) close all upvalues >= r[a - 1]
    int emitJmp(int line, int a, int sBx) {
        emitAsBx(line, OpCode.JMP, a, sBx);
        return insts.size() - 1;
    }

    // if not (r[a] <=> c) then pc++
    void emitTest(int line, int a, int c) {
        emitABC(line, OpCode.TEST, a, 0, c);
    }

    // if (r[b] <=> c) then r[a] = r[b] else pc++
    void emitTestSet(int line, int a, int b, int c) {
        emitABC(line, OpCode.TESTSET, a, b, c);
    }

    int emitForPrep(int line, int a, int sBx) {
        emitAsBx(line, OpCode.FORPREP, a, sBx);
        return insts.size() - 1;
    }

    int emitForLoop(int line, int a, int sBx) {
        emitAsBx(line, OpCode.FORLOOP, a, sBx);
        return insts.size() - 1;
    }

    void emitTForCall(int line, int a, int c) {
        emitABC(line, OpCode.TFORCALL, a, 0, c);
    }

    void emitTForLoop(int line, int a, int sBx) {
        emitAsBx(line, OpCode.TFORLOOP, a, sBx);
    }

    // r[a] = op r[b]
    void emitUnaryOp(int line, TokenKind op, int a, int b) {
        switch (op) {
            case TOKEN_OP_NOT:  emitABC(line, OpCode.NOT,  a, b, 0); break;
            case TOKEN_OP_BNOT: emitABC(line, OpCode.BNOT, a, b, 0); break;
            case TOKEN_OP_LEN:  emitABC(line, OpCode.LEN,  a, b, 0); break;
            case TOKEN_OP_UNM:  emitABC(line, OpCode.UNM,  a, b, 0); break;
        }
    }

    // r[a] = rk[b] op rk[c]
    // arith & bitwise & relational
    void emitBinaryOp(int line, TokenKind op, int a, int b, int c) {
        if (arithAndBitwiseBinops.containsKey(op)) {
            emitABC(line, arithAndBitwiseBinops.get(op), a, b, c);
        } else {
            switch (op) {
                case TOKEN_OP_EQ: emitABC(line, OpCode.EQ, 1, b, c); break;
                case TOKEN_OP_NE: emitABC(line, OpCode.EQ, 0, b, c); break;
                case TOKEN_OP_LT: emitABC(line, OpCode.LT, 1, b, c); break;
                case TOKEN_OP_GT: emitABC(line, OpCode.LT, 1, c, b); break;
                case TOKEN_OP_LE: emitABC(line, OpCode.LE, 1, b, c); break;
                case TOKEN_OP_GE: emitABC(line, OpCode.LE, 1, c, b); break;
            }
            emitJmp(line, 0, 1);
            emitLoadBool(line, a, 0, 1);
            emitLoadBool(line, a, 1, 0);
        }
    }

}
