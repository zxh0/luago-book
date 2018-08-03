package com.github.zxh0.luago.vm;

import com.github.zxh0.luago.api.ArithOp;
import com.github.zxh0.luago.api.CmpOp;
import com.github.zxh0.luago.api.LuaVM;

import static com.github.zxh0.luago.api.ArithOp.*;
import static com.github.zxh0.luago.api.CmpOp.*;
import static com.github.zxh0.luago.api.LuaType.LUA_TSTRING;

public class Instructions {

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
            throw new RuntimeException("todo: jmp!");
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

}
