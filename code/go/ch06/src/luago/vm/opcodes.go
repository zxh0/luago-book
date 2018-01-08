package vm

/* OpMode */
/* basic instruction format */
const (
	IABC  = iota // [  B:9  ][  C:9  ][ A:8  ][OP:6]
	IABx         // [      Bx:18     ][ A:8  ][OP:6]
	IAsBx        // [     sBx:18     ][ A:8  ][OP:6]
	IAx          // [           Ax:26        ][OP:6]
)

/* OpArgMask */
const (
	OpArgN = iota // argument is not used
	OpArgU        // argument is used
	OpArgR        // argument is a register or a jump offset
	OpArgK        // argument is a constant or register/constant
)

/* OpCode */
const (
	OP_MOVE = iota
	OP_LOADK
	OP_LOADKX
	OP_LOADBOOL
	OP_LOADNIL
	OP_GETUPVAL
	OP_GETTABUP
	OP_GETTABLE
	OP_SETTABUP
	OP_SETUPVAL
	OP_SETTABLE
	OP_NEWTABLE
	OP_SELF
	OP_ADD
	OP_SUB
	OP_MUL
	OP_MOD
	OP_POW
	OP_DIV
	OP_IDIV
	OP_BAND
	OP_BOR
	OP_BXOR
	OP_SHL
	OP_SHR
	OP_UNM
	OP_BNOT
	OP_NOT
	OP_LEN
	OP_CONCAT
	OP_JMP
	OP_EQ
	OP_LT
	OP_LE
	OP_TEST
	OP_TESTSET
	OP_CALL
	OP_TAILCALL
	OP_RETURN
	OP_FORLOOP
	OP_FORPREP
	OP_TFORCALL
	OP_TFORLOOP
	OP_SETLIST
	OP_CLOSURE
	OP_VARARG
	OP_EXTRAARG
)

type opcode struct {
	testFlag byte // operator is a test (next instruction must be a jump)
	setAFlag byte // instruction set register A
	argBMode byte // B arg mode
	argCMode byte // C arg mode
	opMode   byte // op mode
	name     string
}

var opcodes = []opcode{
	/*     T  A    B       C     mode         name    */
	opcode{0, 1, OpArgR, OpArgN, IABC /* */, "MOVE    "}, // R(A) := R(B)
	opcode{0, 1, OpArgK, OpArgN, IABx /* */, "LOADK   "}, // R(A) := Kst(Bx)
	opcode{0, 1, OpArgN, OpArgN, IABx /* */, "LOADKX  "}, // R(A) := Kst(extra arg)
	opcode{0, 1, OpArgU, OpArgU, IABC /* */, "LOADBOOL"}, // R(A) := (bool)B; if (C) pc++
	opcode{0, 1, OpArgU, OpArgN, IABC /* */, "LOADNIL "}, // R(A), R(A+1), ..., R(A+B) := nil
	opcode{0, 1, OpArgU, OpArgN, IABC /* */, "GETUPVAL"}, // R(A) := UpValue[B]
	opcode{0, 1, OpArgU, OpArgK, IABC /* */, "GETTABUP"}, // R(A) := UpValue[B][RK(C)]
	opcode{0, 1, OpArgR, OpArgK, IABC /* */, "GETTABLE"}, // R(A) := R(B)[RK(C)]
	opcode{0, 0, OpArgK, OpArgK, IABC /* */, "SETTABUP"}, // UpValue[A][RK(B)] := RK(C)
	opcode{0, 0, OpArgU, OpArgN, IABC /* */, "SETUPVAL"}, // UpValue[B] := R(A)
	opcode{0, 0, OpArgK, OpArgK, IABC /* */, "SETTABLE"}, // R(A)[RK(B)] := RK(C)
	opcode{0, 1, OpArgU, OpArgU, IABC /* */, "NEWTABLE"}, // R(A) := {} (size = B,C)
	opcode{0, 1, OpArgR, OpArgK, IABC /* */, "SELF    "}, // R(A+1) := R(B); R(A) := R(B)[RK(C)]
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "ADD     "}, // R(A) := RK(B) + RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "SUB     "}, // R(A) := RK(B) - RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "MUL     "}, // R(A) := RK(B) * RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "MOD     "}, // R(A) := RK(B) % RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "POW     "}, // R(A) := RK(B) ^ RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "DIV     "}, // R(A) := RK(B) / RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "IDIV    "}, // R(A) := RK(B) // RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "BAND    "}, // R(A) := RK(B) & RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "BOR     "}, // R(A) := RK(B) | RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "BXOR    "}, // R(A) := RK(B) ~ RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "SHL     "}, // R(A) := RK(B) << RK(C)
	opcode{0, 1, OpArgK, OpArgK, IABC /* */, "SHR     "}, // R(A) := RK(B) >> RK(C)
	opcode{0, 1, OpArgR, OpArgN, IABC /* */, "UNM     "}, // R(A) := -R(B)
	opcode{0, 1, OpArgR, OpArgN, IABC /* */, "BNOT    "}, // R(A) := ~R(B)
	opcode{0, 1, OpArgR, OpArgN, IABC /* */, "NOT     "}, // R(A) := not R(B)
	opcode{0, 1, OpArgR, OpArgN, IABC /* */, "LEN     "}, // R(A) := length of R(B)
	opcode{0, 1, OpArgR, OpArgR, IABC /* */, "CONCAT  "}, // R(A) := R(B).. ... ..R(C)
	opcode{0, 0, OpArgR, OpArgN, IAsBx /**/, "JMP     "}, // pc+=sBx; if (A) close all upvalues >= R(A - 1)
	opcode{1, 0, OpArgK, OpArgK, IABC /* */, "EQ      "}, // if ((RK(B) == RK(C)) ~= A) then pc++
	opcode{1, 0, OpArgK, OpArgK, IABC /* */, "LT      "}, // if ((RK(B) <  RK(C)) ~= A) then pc++
	opcode{1, 0, OpArgK, OpArgK, IABC /* */, "LE      "}, // if ((RK(B) <= RK(C)) ~= A) then pc++
	opcode{1, 0, OpArgN, OpArgU, IABC /* */, "TEST    "}, // if not (R(A) <=> C) then pc++
	opcode{1, 1, OpArgR, OpArgU, IABC /* */, "TESTSET "}, // if (R(B) <=> C) then R(A) := R(B) else pc++
	opcode{0, 1, OpArgU, OpArgU, IABC /* */, "CALL    "}, // R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
	opcode{0, 1, OpArgU, OpArgU, IABC /* */, "TAILCALL"}, // return R(A)(R(A+1), ... ,R(A+B-1))
	opcode{0, 0, OpArgU, OpArgN, IABC /* */, "RETURN  "}, // return R(A), ... ,R(A+B-2)
	opcode{0, 1, OpArgR, OpArgN, IAsBx /**/, "FORLOOP "}, // R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }
	opcode{0, 1, OpArgR, OpArgN, IAsBx /**/, "FORPREP "}, // R(A)-=R(A+2); pc+=sBx
	opcode{0, 0, OpArgN, OpArgU, IABC /* */, "TFORCALL"}, // R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));
	opcode{0, 1, OpArgR, OpArgN, IAsBx /**/, "TFORLOOP"}, // if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx }
	opcode{0, 0, OpArgU, OpArgU, IABC /* */, "SETLIST "}, // R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
	opcode{0, 1, OpArgU, OpArgN, IABx /* */, "CLOSURE "}, // R(A) := closure(KPROTO[Bx])
	opcode{0, 1, OpArgU, OpArgN, IABC /* */, "VARARG  "}, // R(A), R(A+1), ..., R(A+B-2) = vararg
	opcode{0, 0, OpArgU, OpArgU, IAx /*  */, "EXTRAARG"}, // extra (larger) argument for previous opcode
}
