package com.github.zxh0.luago.vm;

import lombok.Getter;

import static com.github.zxh0.luago.vm.OpArgMask.*;
import static com.github.zxh0.luago.vm.OpMode.*;

@Getter
public enum OpCode {

    /*       T  A    B       C     mode */
    MOVE    (0, 1, OpArgR, OpArgN, iABC ), // R(A) := R(B)
    LOADK   (0, 1, OpArgK, OpArgN, iABx ), // R(A) := Kst(Bx)
    LOADKX  (0, 1, OpArgN, OpArgN, iABx ), // R(A) := Kst(extra arg)
    LOADBOOL(0, 1, OpArgU, OpArgU, iABC ), // R(A) := (bool)B; if (C) pc++
    LOADNIL (0, 1, OpArgU, OpArgN, iABC ), // R(A), R(A+1), ..., R(A+B) := nil
    GETUPVAL(0, 1, OpArgU, OpArgN, iABC ), // R(A) := UpValue[B]
    GETTABUP(0, 1, OpArgU, OpArgK, iABC ), // R(A) := UpValue[B][RK(C)]
    GETTABLE(0, 1, OpArgR, OpArgK, iABC ), // R(A) := R(B)[RK(C)]
    SETTABUP(0, 0, OpArgK, OpArgK, iABC ), // UpValue[A][RK(B)] := RK(C)
    SETUPVAL(0, 0, OpArgU, OpArgN, iABC ), // UpValue[B] := R(A)
    SETTABLE(0, 0, OpArgK, OpArgK, iABC ), // R(A)[RK(B)] := RK(C)
    NEWTABLE(0, 1, OpArgU, OpArgU, iABC ), // R(A) := {} (size = B,C)
    SELF    (0, 1, OpArgR, OpArgK, iABC ), // R(A+1) := R(B); R(A) := R(B)[RK(C)]
    ADD     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) + RK(C)
    SUB     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) - RK(C)
    MUL     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) * RK(C)
    MOD     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) % RK(C)
    POW     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) ^ RK(C)
    DIV     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) / RK(C)
    IDIV    (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) // RK(C)
    BAND    (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) & RK(C)
    BOR     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) | RK(C)
    BXOR    (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) ~ RK(C)
    SHL     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) << RK(C)
    SHR     (0, 1, OpArgK, OpArgK, iABC ), // R(A) := RK(B) >> RK(C)
    UNM     (0, 1, OpArgR, OpArgN, iABC ), // R(A) := -R(B)
    BNOT    (0, 1, OpArgR, OpArgN, iABC ), // R(A) := ~R(B)
    NOT     (0, 1, OpArgR, OpArgN, iABC ), // R(A) := not R(B)
    LEN     (0, 1, OpArgR, OpArgN, iABC ), // R(A) := length of R(B)
    CONCAT  (0, 1, OpArgR, OpArgR, iABC ), // R(A) := R(B).. ... ..R(C)
    JMP     (0, 0, OpArgR, OpArgN, iAsBx), // pc+=sBx; if (A) close all upvalues >= R(A - 1)
    EQ      (1, 0, OpArgK, OpArgK, iABC ), // if ((RK(B) == RK(C)) ~= A) then pc++
    LT      (1, 0, OpArgK, OpArgK, iABC ), // if ((RK(B) <  RK(C)) ~= A) then pc++
    LE      (1, 0, OpArgK, OpArgK, iABC ), // if ((RK(B) <= RK(C)) ~= A) then pc++
    TEST    (1, 0, OpArgN, OpArgU, iABC ), // if not (R(A) <=> C) then pc++
    TESTSET (1, 1, OpArgR, OpArgU, iABC ), // if (R(B) <=> C) then R(A) := R(B) else pc++
    CALL    (0, 1, OpArgU, OpArgU, iABC ), // R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
    TAILCALL(0, 1, OpArgU, OpArgU, iABC ), // return R(A)(R(A+1), ... ,R(A+B-1))
    RETURN  (0, 0, OpArgU, OpArgN, iABC ), // return R(A), ... ,R(A+B-2)
    FORLOOP (0, 1, OpArgR, OpArgN, iAsBx), // R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }
    FORPREP (0, 1, OpArgR, OpArgN, iAsBx), // R(A)-=R(A+2); pc+=sBx
    TFORCALL(0, 0, OpArgN, OpArgU, iABC ), // R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));
    TFORLOOP(0, 1, OpArgR, OpArgN, iAsBx), // if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx }
    SETLIST (0, 0, OpArgU, OpArgU, iABC ), // R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
    CLOSURE (0, 1, OpArgU, OpArgN, iABx ), // R(A) := closure(KPROTO[Bx])
    VARARG  (0, 1, OpArgU, OpArgN, iABC ), // R(A), R(A+1), ..., R(A+B-2) = vararg
    EXTRAARG(0, 0, OpArgU, OpArgU, iAx  ), // extra (larger) argument for previous opcode
    ;


    private final int testFlag; // operator is a test (next instruction must be a jump)
    private final int setAFlag; // instruction set register A
    private final OpArgMask argBMode; // B arg mode
    private final OpArgMask argCMode; // C arg mode
    private final OpMode opMode; // op mode

    OpCode(int testFlag, int setAFlag,
           OpArgMask argBMode, OpArgMask argCMode, OpMode opMode) {
        this.testFlag = testFlag;
        this.setAFlag = setAFlag;
        this.argBMode = argBMode;
        this.argCMode = argCMode;
        this.opMode = opMode;
    }

}
