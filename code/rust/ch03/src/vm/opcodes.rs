/* OpCode */
// const OP_MOVE: u8 = 0x00;
// const OP_LOADK: u8 = 0x01;
// const OP_LOADKX: u8 = 0x02;
// const OP_LOADBOOL: u8 = 0x03;
// const OP_LOADNIL: u8 = 0x04;
// const OP_GETUPVAL: u8 = 0x05;
// const OP_GETTABUP: u8 = 0x06;
// const OP_GETTABLE: u8 = 0x07;
// const OP_SETTABUP: u8 = 0x08;
// const OP_SETUPVAL: u8 = 0x09;
// const OP_SETTABLE: u8 = 0x0a;
// const OP_NEWTABLE: u8 = 0x0b;
// const OP_SELF: u8 = 0x0c;
// const OP_ADD: u8 = 0x0d;
// const OP_SUB: u8 = 0x0e;
// const OP_MUL: u8 = 0x0f;
// const OP_MOD: u8 = 0x10;
// const OP_POW: u8 = 0x11;
// const OP_DIV: u8 = 0x12;
// const OP_IDIV: u8 = 0x13;
// const OP_BAND: u8 = 0x14;
// const OP_BOR: u8 = 0x15;
// const OP_BXOR: u8 = 0x16;
// const OP_SHL: u8 = 0x17;
// const OP_SHR: u8 = 0x18;
// const OP_UNM: u8 = 0x19;
// const OP_BNOT: u8 = 0x1a;
// const OP_NOT: u8 = 0x1b;
// const OP_LEN: u8 = 0x1c;
// const OP_CONCAT: u8 = 0x1d;
// const OP_JMP: u8 = 0x1e;
// const OP_EQ: u8 = 0x1f;
// const OP_LT: u8 = 0x20;
// const OP_LE: u8 = 0x21;
// const OP_TEST: u8 = 0x22;
// const OP_TESTSET: u8 = 0x23;
// const OP_CALL: u8 = 0x24;
// const OP_TAILCALL: u8 = 0x25;
// const OP_RETURN: u8 = 0x26;
// const OP_FORLOOP: u8 = 0x27;
// const OP_FORPREP: u8 = 0x28;
// const OP_TFORCALL: u8 = 0x29;
// const OP_TFORLOOP: u8 = 0x2a;
// const OP_SETLIST: u8 = 0x2b;
// const OP_CLOSURE: u8 = 0x2c;
// const OP_VARARG: u8 = 0x2d;
// const OP_EXTRAARG: u8 = 0x2e;

/* OpMode */
pub const OP_MODE_ABC: u8 = 0; // iABC
pub const OP_MODE_ABX: u8 = 1; // iABx
pub const OP_MODE_ASBX: u8 = 2; // iAsBx
pub const OP_MODE_AX: u8 = 3; // iAx

/* OpArgMask */
pub const OP_ARG_N: u8 = 0; // OpArgN
pub const OP_ARG_U: u8 = 1; // OpArgU
pub const OP_ARG_R: u8 = 2; // OpArgR
pub const OP_ARG_K: u8 = 3; // OpArgK

pub const OPCODES: &'static [OpCode] = &[
    /*       B       C     mode    name    */
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ABC, "MOVE    "), // R(A) := R(B)
    opcode(OP_ARG_K, OP_ARG_N, OP_MODE_ABX, "LOADK   "), // R(A) := Kst(Bx)
    opcode(OP_ARG_N, OP_ARG_N, OP_MODE_ABX, "LOADKX  "), // R(A) := Kst(extra arg)
    opcode(OP_ARG_U, OP_ARG_U, OP_MODE_ABC, "LOADBOOL"), // R(A) := (bool)B; if (C) pc++
    opcode(OP_ARG_U, OP_ARG_N, OP_MODE_ABC, "LOADNIL "), // R(A), R(A+1), ..., R(A+B) := nil
    opcode(OP_ARG_U, OP_ARG_N, OP_MODE_ABC, "GETUPVAL"), // R(A) := UpValue[B]
    opcode(OP_ARG_U, OP_ARG_K, OP_MODE_ABC, "GETTABUP"), // R(A) := UpValue[B][RK(C)]
    opcode(OP_ARG_R, OP_ARG_K, OP_MODE_ABC, "GETTABLE"), // R(A) := R(B)[RK(C)]
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "SETTABUP"), // UpValue[A][RK(B)] := RK(C)
    opcode(OP_ARG_U, OP_ARG_N, OP_MODE_ABC, "SETUPVAL"), // UpValue[B] := R(A)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "SETTABLE"), // R(A)[RK(B)] := RK(C)
    opcode(OP_ARG_U, OP_ARG_U, OP_MODE_ABC, "NEWTABLE"), // R(A) := {} (size = B,C)
    opcode(OP_ARG_R, OP_ARG_K, OP_MODE_ABC, "SELF    "), // R(A+1) := R(B); R(A) := R(B)[RK(C)]
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "ADD     "), // R(A) := RK(B) + RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "SUB     "), // R(A) := RK(B) - RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "MUL     "), // R(A) := RK(B) * RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "MOD     "), // R(A) := RK(B) % RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "POW     "), // R(A) := RK(B) ^ RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "DIV     "), // R(A) := RK(B) / RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "IDIV    "), // R(A) := RK(B) // RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "BAND    "), // R(A) := RK(B) & RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "BOR     "), // R(A) := RK(B) | RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "BXOR    "), // R(A) := RK(B) ~ RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "SHL     "), // R(A) := RK(B) << RK(C)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "SHR     "), // R(A) := RK(B) >> RK(C)
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ABC, "UNM     "), // R(A) := -R(B)
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ABC, "BNOT    "), // R(A) := ~R(B)
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ABC, "NOT     "), // R(A) := not R(B)
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ABC, "LEN     "), // R(A) := length of R(B)
    opcode(OP_ARG_R, OP_ARG_R, OP_MODE_ABC, "CONCAT  "), // R(A) := R(B).. ... ..R(C)
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ASBX, "JMP     "), // pc+=sBx; if (A) close all upvalues >= R(A - 1)
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "EQ      "), // if ((RK(B) == RK(C)) ~= A) then pc++
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "LT      "), // if ((RK(B) <  RK(C)) ~= A) then pc++
    opcode(OP_ARG_K, OP_ARG_K, OP_MODE_ABC, "LE      "), // if ((RK(B) <= RK(C)) ~= A) then pc++
    opcode(OP_ARG_N, OP_ARG_U, OP_MODE_ABC, "TEST    "), // if not (R(A) <=> C) then pc++
    opcode(OP_ARG_R, OP_ARG_U, OP_MODE_ABC, "TESTSET "), // if (R(B) <=> C) then R(A) := R(B) else pc++
    opcode(OP_ARG_U, OP_ARG_U, OP_MODE_ABC, "CALL    "), // R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
    opcode(OP_ARG_U, OP_ARG_U, OP_MODE_ABC, "TAILCALL"), // return R(A)(R(A+1), ... ,R(A+B-1))
    opcode(OP_ARG_U, OP_ARG_N, OP_MODE_ABC, "RETURN  "), // return R(A), ... ,R(A+B-2)
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ASBX, "FORLOOP "), // R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ASBX, "FORPREP "), // R(A)-=R(A+2); pc+=sBx
    opcode(OP_ARG_N, OP_ARG_U, OP_MODE_ABC, "TFORCALL"),  // R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));
    opcode(OP_ARG_R, OP_ARG_N, OP_MODE_ASBX, "TFORLOOP"), // if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx }
    opcode(OP_ARG_U, OP_ARG_U, OP_MODE_ABC, "SETLIST "),  // R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
    opcode(OP_ARG_U, OP_ARG_N, OP_MODE_ABX, "CLOSURE "),  // R(A) := closure(KPROTO[Bx])
    opcode(OP_ARG_U, OP_ARG_N, OP_MODE_ABC, "VARARG  "),  // R(A), R(A+1), ..., R(A+B-2) = vararg
    opcode(OP_ARG_U, OP_ARG_U, OP_MODE_AX, "EXTRAARG"),   // extra (larger) argument for previous opcode
];

const fn opcode(bmode: u8, cmode: u8, opmode: u8, name: &'static str) -> OpCode {
    OpCode {
        bmode,
        cmode,
        opmode,
        name,
    }
}

pub struct OpCode {
    pub bmode: u8,  // B arg mode
    pub cmode: u8,  // C arg mode
    pub opmode: u8, // op mode
    pub name: &'static str,
}
