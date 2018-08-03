package com.github.zxh0.luago.vm;

import com.github.zxh0.luago.api.ArithOp;
import com.github.zxh0.luago.api.CmpOp;
import com.github.zxh0.luago.api.LuaVM;

import static com.github.zxh0.luago.api.ArithOp.*;
import static com.github.zxh0.luago.api.CmpOp.*;
import static com.github.zxh0.luago.api.LuaState.LUA_REGISTRYINDEX;
import static com.github.zxh0.luago.api.LuaType.LUA_TSTRING;

public class Instructions {

    /* number of list items to accumulate before a SETLIST instruction */
    public static final int LFIELDS_PER_FLUSH = 50;

    /* misc */

    // R(A) := R(B)
    public static void move(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.copy(b, a);
    }

    // pc+=sBx; if (A) close all upvalues >= R(A - 1)
    public static void jmp(int i, LuaVM vm) {
        int a = Instruction.getA(i);
        int sBx = Instruction.getSBx(i);
        vm.addPC(sBx);
        if (a != 0) {
            vm.closeUpvalues(a);
        }
    }

    /* load */

    // R(A), R(A+1), ..., R(A+B) := nil
    public static void loadNil(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        vm.pushNil();
        for (int j = a; j <= a+b; j++) {
            vm.copy(-1, j);
        }
        vm.pop(1);
    }

    // R(A) := (bool)B; if (C) pc++
    public static void loadBool(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.pushBoolean(b != 0);
        vm.replace(a);
        if (c != 0) {
            vm.addPC(1);
        }
    }

    // R(A) := Kst(Bx)
    public static void loadK(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int bx = Instruction.getBx(i);
        vm.getConst(bx);
        vm.replace(a);
    }

    // R(A) := Kst(extra arg)
    public static void loadKx(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int ax = Instruction.getAx(vm.fetch());
        vm.getConst(ax);
        vm.replace(a);
    }

    /* arith */

    public static void add (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPADD ); } // +
    public static void sub (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPSUB ); } // -
    public static void mul (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPMUL ); } // *
    public static void mod (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPMOD ); } // %
    public static void pow (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPPOW ); } // ^
    public static void div (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPDIV ); } // /
    public static void idiv(int i, LuaVM vm) { binaryArith(i, vm, LUA_OPIDIV); } // //
    public static void band(int i, LuaVM vm) { binaryArith(i, vm, LUA_OPBAND); } // &
    public static void bor (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPBOR ); } // |
    public static void bxor(int i, LuaVM vm) { binaryArith(i, vm, LUA_OPBXOR); } // ~
    public static void shl (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPSHL ); } // <<
    public static void shr (int i, LuaVM vm) { binaryArith(i, vm, LUA_OPSHR ); } // >>
    public static void unm (int i, LuaVM vm) { unaryArith( i, vm, LUA_OPUNM ); } // -
    public static void bnot(int i, LuaVM vm) { unaryArith( i, vm, LUA_OPBNOT); } // ~

    // R(A) := RK(B) op RK(C)
    private static void binaryArith(int i, LuaVM vm, ArithOp op) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.getRK(b);
        vm.getRK(c);
        vm.arith(op);
        vm.replace(a);
    }

    // R(A) := op R(B)
    private static void unaryArith(int i, LuaVM vm, ArithOp op) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.pushValue(b);
        vm.arith(op);
        vm.replace(a);
    }

    /* compare */

    public static void eq(int i, LuaVM vm) { compare(i, vm, LUA_OPEQ); } // ==
    public static void lt(int i, LuaVM vm) { compare(i, vm, LUA_OPLT); } // <
    public static void le(int i, LuaVM vm) { compare(i, vm, LUA_OPLE); } // <=

    // if ((RK(B) op RK(C)) ~= A) then pc++
    private static void compare(int i, LuaVM vm, CmpOp op) {
        int a = Instruction.getA(i);
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.getRK(b);
        vm.getRK(c);
        if (vm.compare(-2, -1, op) != (a != 0)) {
            vm.addPC(1);
        }
        vm.pop(2);
    }

    /* logical */

    // R(A) := not R(B)
    public static void not(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.pushBoolean(!vm.toBoolean(b));
        vm.replace(a);
    }

    // if not (R(A) <=> C) then pc++
    public static void test(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int c = Instruction.getC(i);
        if (vm.toBoolean(a) != (c != 0)) {
            vm.addPC(1);
        }
    }

    // if (R(B) <=> C) then R(A) := R(B) else pc++
    public static void testSet(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        int c = Instruction.getC(i);
        if (vm.toBoolean(b) == (c != 0)) {
            vm.copy(b, a);
        } else {
            vm.addPC(1);
        }
    }

    /* len & concat */

    // R(A) := length of R(B)
    public static void length(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.len(b);
        vm.replace(a);
    }

    // R(A) := R(B).. ... ..R(C)
    public static void concat(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        int c = Instruction.getC(i) + 1;
        int n = c - b + 1;
        vm.checkStack(n);
        for (int j = b; j <= c; j++) {
            vm.pushValue(j);
        }
        vm.concat(n);
        vm.replace(a);
    }

    /* for */

    // R(A)-=R(A+2); pc+=sBx
    public static void forPrep(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int sBx = Instruction.getSBx(i);

        if (vm.type(a) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a));
            vm.replace(a);
        }
        if (vm.type(a+1) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a + 1));
            vm.replace(a + 1);
        }
        if (vm.type(a+2) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a + 2));
            vm.replace(a + 2);
        }

        vm.pushValue(a);
        vm.pushValue(a + 2);
        vm.arith(LUA_OPSUB);
        vm.replace(a);
        vm.addPC(sBx);
    }

    // R(A)+=R(A+2);
    // if R(A) <?= R(A+1) then {
    //   pc+=sBx; R(A+3)=R(A)
    // }
    public static void forLoop(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int sBx = Instruction.getSBx(i);

        // R(A)+=R(A+2);
        vm.pushValue(a + 2);
        vm.pushValue(a);
        vm.arith(LUA_OPADD);
        vm.replace(a);

        boolean isPositiveStep = vm.toNumber(a+2) >= 0;
        if (isPositiveStep && vm.compare(a, a+1, LUA_OPLE) ||
                !isPositiveStep && vm.compare(a+1, a, LUA_OPLE)) {
            // pc+=sBx; R(A+3)=R(A)
            vm.addPC(sBx);
            vm.copy(a, a+3);
        }
    }

    /* table */

    // R(A) := {} (size = B,C)
    public static void newTable(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.createTable(FPB.fb2int(b), FPB.fb2int(c));
        vm.replace(a);
    }

    // R(A) := R(B)[RK(C)]
    public static void getTable(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        int c = Instruction.getC(i);
        vm.getRK(c);
        vm.getTable(b);
        vm.replace(a);
    }

    // R(A)[RK(B)] := RK(C)
    public static void setTable(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.getRK(b);
        vm.getRK(c);
        vm.setTable(a);
    }

    // R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
    public static void setList(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        c = c > 0 ? c - 1 : Instruction.getAx(vm.fetch());

        boolean bIsZero = b == 0;
        if (bIsZero) {
            b = ((int) vm.toInteger(-1)) - a - 1;
            vm.pop(1);
        }

        vm.checkStack(1);
        int idx = c * LFIELDS_PER_FLUSH;
        for (int j = 1; j <= b; j++) {
            idx++;
            vm.pushValue(a + j);
            vm.setI(a, idx);
        }

        if (bIsZero) {
            for (int j = vm.registerCount() + 1; j <= vm.getTop(); j++) {
                idx++;
                vm.pushValue(j);
                vm.setI(a, idx);
            }

            // clear stack
            vm.setTop(vm.registerCount());
        }
    }

    /* call */

    // R(A+1) := R(B); R(A) := R(B)[RK(C)]
    public static void self(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        int c = Instruction.getC(i);
        vm.copy(b, a+1);
        vm.getRK(c);
        vm.getTable(b);
        vm.replace(a);
    }

    // R(A) := closure(KPROTO[Bx])
    public static void closure(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int bx = Instruction.getBx(i);
        vm.loadProto(bx);
        vm.replace(a);
    }

    // R(A), R(A+1), ..., R(A+B-2) = vararg
    public static void vararg(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        if (b != 1) { // b==0 or b>1
            vm.loadVararg(b - 1);
            popResults(a, b, vm);
        }
    }

    // return R(A)(R(A+1), ... ,R(A+B-1))
    public static void tailCall(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        // todo: optimize tail call!
        int c = 0;
        int nArgs = pushFuncAndArgs(a, b, vm);
        vm.call(nArgs, c-1);
        popResults(a, c, vm);
    }

    // R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
    public static void call(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        int nArgs = pushFuncAndArgs(a, b, vm);
        vm.call(nArgs, c-1);
        popResults(a, c, vm);
    }

    // return R(A), ... ,R(A+B-2)
    public static void _return(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        if (b == 1) {
            // no return values
        } else if (b > 1) {
            // b-1 return values
            vm.checkStack(b - 1);
            for (int j = a; j <= a+b-2; j++) {
                vm.pushValue(j);
            }
        } else {
            fixStack(a, vm);
        }
    }

    private static int pushFuncAndArgs(int a, int b, LuaVM vm) {
        if (b >= 1) {
            vm.checkStack(b);
            for (int i = a; i < a+b; i++) {
                vm.pushValue(i);
            }
            return b - 1;
        } else {
            fixStack(a, vm);
            return vm.getTop() - vm.registerCount() - 1;
        }
    }

    private static void fixStack(int a, LuaVM vm) {
        int x = (int) vm.toInteger(-1);
        vm.pop(1);

        vm.checkStack(x - a);
        for (int i = a; i < x; i++) {
            vm.pushValue(i);
        }
        vm.rotate(vm.registerCount()+1, x-a);
    }

    private static void popResults(int a, int c, LuaVM vm) {
        if (c == 1) {
            // no results
        } else if (c > 1) {
            for (int i = a + c - 2; i >= a; i--) {
                vm.replace(i);
            }
        } else {
            // leave results on stack
            vm.checkStack(1);
            vm.pushInteger(a);
        }
    }

    /* upvalues */

    // R(A) := UpValue[B]
    public static void getUpval(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.copy(luaUpvalueIndex(b), a);
    }

    // UpValue[B] := R(A)
    public static void setUpval(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.copy(a, luaUpvalueIndex(b));
    }

    // R(A) := UpValue[B][RK(C)]
    public static void getTabUp(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        int c = Instruction.getC(i);
        vm.getRK(c);
        vm.getTable(luaUpvalueIndex(b));
        vm.replace(a);
    }

    // UpValue[A][RK(B)] := RK(C)
    public static void setTabUp(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.getRK(b);
        vm.getRK(c);
        vm.setTable(luaUpvalueIndex(a));
    }

    private static int luaUpvalueIndex(int i) {
        return LUA_REGISTRYINDEX - i;
    }

}
