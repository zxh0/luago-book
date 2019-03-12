using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    internal struct opcode
    {
        internal byte testFlag;
        internal byte setAFlag;
        internal byte argBMode;
        internal byte argCMode;
        internal byte opMode;
        internal string name;
        internal Action action;

        public opcode(byte testFlag, byte aFlag, byte argBMode, byte argCMode, byte opMode, string name, Action action)
        {
            this.testFlag = testFlag;
            setAFlag = aFlag;
            this.argBMode = argBMode;
            this.argCMode = argCMode;
            this.opMode = opMode;
            this.name = name;
            this.action = action;
        }
    }

    delegate void Action(Instruction i, ref LuaVM vm);

    public static class OpCodes
    {
        private const byte IABC = 0;
        private const byte IABx = 1;
        private const byte IAsBx = 2;
        private const byte IAx = 3;

        internal const byte OP_MOVE = 0;
        internal const byte OP_LOADK = 1;
        internal const byte OP_LOADKX = 2;
        internal const byte OP_LOADBOOL = 3;
        internal const byte OP_LOADNIL = 4;
        internal const byte OP_GETUPVAL = 5;
        internal const byte OP_GETTABUP = 6;
        internal const byte OP_GETTABLE = 7;
        internal const byte OP_SETTABUP = 8;
        internal const byte OP_SETUPVAL = 9;
        internal const byte OP_SETTABLE = 10;
        internal const byte OP_NEWTABLE = 11;
        internal const byte OP_SELF = 12;
        internal const byte OP_ADD = 13;
        internal const byte OP_SUB = 14;
        internal const byte OP_MUL = 15;
        internal const byte OP_MOD = 16;
        internal const byte OP_POW = 17;
        internal const byte OP_DIV = 18;
        internal const byte OP_IDIV = 19;
        internal const byte OP_BAND = 20;
        internal const byte OP_BOR = 21;
        internal const byte OP_BXOR = 22;
        internal const byte OP_SHL = 23;
        internal const byte OP_SHR = 24;
        internal const byte OP_UNM = 25;
        internal const byte OP_BNOT = 26;
        internal const byte OP_NOT = 27;
        internal const byte OP_LEN = 28;
        internal const byte OP_CONCAT = 29;
        internal const byte OP_JMP = 30;
        internal const byte OP_EQ = 31;
        internal const byte OP_LT = 32;
        internal const byte OP_LE = 33;
        internal const byte OP_TEST = 34;
        internal const byte OP_TESTSET = 35;
        internal const byte OP_CALL = 36;
        internal const byte OP_TAILCALL = 37;
        internal const byte OP_RETURN = 38;
        internal const byte OP_FORLOOP = 39;
        internal const byte OP_FORPREP = 40;
        internal const byte OP_TFORCALL = 41;
        internal const byte OP_TFORLOOP = 42;
        internal const byte OP_SETLIST = 43;
        internal const byte OP_CLOSURE = 44;
        internal const byte OP_VARARG = 45;
        internal const byte OP_EXTRAARG = 46;

        internal const byte OpArgN = 0;
        internal const byte OpArgU = 1;
        internal const byte OpArgR = 2;
        internal const byte OpArgK = 3;

        internal static opcode[] opcodes =
        {
            /*   T            A             B                     C                   mode         name              action*/
            new opcode(0, 1, OpArgR, OpArgN, IABC, "MOVE    ", InstMisc.move), // R(A) := R(B)
            new opcode(0, 1, OpArgK, OpArgN, IABx, "LOADK   ", InstLoad.loadK), // R(A) := Kst(Bx)
            new opcode(0, 1, OpArgN, OpArgN, IABx, "LOADKX  ", InstLoad.loadKx), // R(A) := Kst(extra arg)
            new opcode(0, 1, OpArgU, OpArgU, IABC, "LOADBOOL", InstLoad.loadBool), // R(A) := (bool)B; if (C) pc++
            new opcode(0, 1, OpArgU, OpArgN, IABC, "LOADNIL ",
                InstLoad.loadNil), // R(A), R(A+1), ..., R(A+B) := nil
            new opcode(0, 1, OpArgU, OpArgN, IABC, "GETUPVAL", null), // R(A) := UpValue[B]
            new opcode(0, 1, OpArgU, OpArgK, IABC, "GETTABUP", InstUpvalue.getTabUp), // R(A) := UpValue[B][RK(C)]
            new opcode(0, 1, OpArgR, OpArgK, IABC /* */, "GETTABLE", InstTable.getTable), // R(A) := R(B)[RK(C)]
            new opcode(0, 0, OpArgK, OpArgK, IABC /* */, "SETTABUP", null), // UpValue[A][RK(B)] := RK(C)
            new opcode(0, 0, OpArgU, OpArgN, IABC /* */, "SETUPVAL", null), // UpValue[B] := R(A)
            new opcode(0, 0, OpArgK, OpArgK, IABC /* */, "SETTABLE", InstTable.setTable), // R(A)[RK(B)] := RK(C)
            new opcode(0, 1, OpArgU, OpArgU, IABC /* */, "NEWTABLE", InstTable.newTable), // R(A) := {} (size = B,C)
            new opcode(0, 1, OpArgR, OpArgK, IABC /* */, "SELF    ", null), // R(A+1) := R(B); R(A) := R(B)[RK(C)]
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "ADD     ", InstOperators.add), // R(A) := RK(B) + RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "SUB     ", InstOperators.sub), // R(A) := RK(B) - RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "MUL     ", InstOperators.mul), // R(A) := RK(B) * RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "MOD     ", InstOperators.mod), // R(A) := RK(B) % RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "POW     ", InstOperators.pow), // R(A) := RK(B) ^ RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "DIV     ", InstOperators.div), // R(A) := RK(B) / RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "IDIV    ", InstOperators.idiv), // R(A) := RK(B) // RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "BAND    ", InstOperators.band), // R(A) := RK(B) & RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "BOR     ", InstOperators.bor), // R(A) := RK(B) | RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "BXOR    ", InstOperators.bxor), // R(A) := RK(B) ~ RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "SHL     ", InstOperators.shl), // R(A) := RK(B) << RK(C)
            new opcode(0, 1, OpArgK, OpArgK, IABC /* */, "SHR     ", InstOperators.shr), // R(A) := RK(B) >> RK(C)
            new opcode(0, 1, OpArgR, OpArgN, IABC /* */, "UNM     ", InstOperators.unm), // R(A) := -R(B)
            new opcode(0, 1, OpArgR, OpArgN, IABC /* */, "BNOT    ", InstOperators.bnot), // R(A) := ~R(B)
            new opcode(0, 1, OpArgR, OpArgN, IABC /* */, "NOT     ", InstOperators.not), // R(A) := not R(B)
            new opcode(0, 1, OpArgR, OpArgN, IABC /* */, "LEN     ",
                InstOperators.length), // R(A) := length of R(B)
            new opcode(0, 1, OpArgR, OpArgR, IABC /* */, "CONCAT  ",
                InstOperators.concat), // R(A) := R(B).. ... ..R(C)
            new opcode(0, 0, OpArgR, OpArgN, IAsBx, "JMP     ", InstMisc.jmp),
            new opcode(1, 0, OpArgK, OpArgK, IABC, "EQ      ", InstOperators.eq),
            new opcode(1, 0, OpArgK, OpArgK, IABC, "LT      ", InstOperators.lt),
            new opcode(1, 0, OpArgK, OpArgK, IABC, "LE      ", InstOperators.le),
            new opcode(1, 0, OpArgN, OpArgU, IABC, "TEST    ", InstOperators.test),
            new opcode(1, 1, OpArgR, OpArgU, IABC, "TESTSET ", InstOperators.testSet),
            new opcode(0, 1, OpArgU, OpArgU, IABC /* */, "CALL    ", InstCall.call),
            new opcode(0, 1, OpArgU, OpArgU, IABC /* */, "TAILCALL",
                InstCall.tailCall), // return R(A)(R(A+1), ... ,R(A+B-1))
            new opcode(0, 0, OpArgU, OpArgN, IABC /* */, "RETURN  ",
                InstCall._return), // return R(A), ... ,R(A+B-2)
            new opcode(0, 1, OpArgR, OpArgN, IAsBx /**/, "FORLOOP ", InstFor.forLoop),
            new opcode(0, 1, OpArgR, OpArgN, IAsBx /**/, "FORPREP ", InstFor.forPrep), // R(A)-=R(A+2); pc+=sBx
            new opcode(0, 0, OpArgN, OpArgU, IABC /* */, "TFORCALL", null),
            new opcode(0, 1, OpArgR, OpArgN, IAsBx /**/, "TFORLOOP", null),
            new opcode(0, 0, OpArgU, OpArgU, IABC /* */, "SETLIST ", InstTable.setList),
            new opcode(0, 1, OpArgU, OpArgN, IABx /* */, "CLOSURE ", InstCall.closure),
            new opcode(0, 1, OpArgU, OpArgN, IABC /* */, "VARARG  ", InstCall.vararg),
            new opcode(0, 0, OpArgU, OpArgU, IAx /*  */, "EXTRAARG", null),
        };
    }
}