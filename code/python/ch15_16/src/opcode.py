from arith_op import ArithOp
from cmp_op import CmpOp
from lua_type import LuaType
from lua_value import LuaValue
from consts import Consts


# OpMode
IABC = 0
IABx = 1
IAsBx = 2
IAx = 3

# OpArg
OpArgN = 0
OpArgU = 1
OpArgR = 2
OpArgK = 3


LFIELDS_PER_FLUSH = 50


class OpCode:
    MOVE = 0
    LOADK = 1
    LOADKX = 2
    LOADBOOL = 3
    LOADNIL = 4
    GETUPVAL = 5
    GETTABUP = 6
    GETTABLE = 7
    SETTABUP = 8
    SETUPVAL = 9
    SETTABLE = 10
    NEWTABLE = 11
    SELF = 12
    ADD = 13
    SUB = 14
    MUL = 15
    MOD = 16
    POW = 17
    DIV = 18
    IDIV = 19
    BAND = 20
    BOR = 21
    BXOR = 22
    SHL = 23
    SHR = 24
    UNM = 25
    BNOT = 26
    NOT = 27
    LEN = 28
    CONCAT = 29
    JMP = 30
    EQ = 31
    LT = 32
    LE = 33
    TEST = 34
    TESTSET = 35
    CALL = 36
    TAILCALL = 37
    RETURN = 38
    FORLOOP = 39
    FORPREP = 40
    TFORCALL = 41
    TFORLOOP = 42
    SETLIST = 43
    CLOSURE = 44
    VARARG = 45
    EXTRAARG = 46

    def __init__(self, test_flag, set_a_flag, arg_b_mode, arg_c_mode, op_mode, name, action):
        self.test_flag = test_flag
        self.set_a_flag = set_a_flag
        self.arg_b_mode = arg_b_mode
        self.arg_c_mode = arg_c_mode
        self.op_mode = op_mode
        self.name = name
        self.action = action


# R(A) := R(B)
def move(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    b += 1
    vm.copy(b, a)


# R(A) := Kst(Bx)
def loadk(inst, vm):
    a, bx = inst.a_bx()
    a += 1
    vm.get_const(bx)
    vm.replace(a)


# R(A) := Kst(extra arg)
def loadkx(inst, vm):
    a, _ = inst.a_bx()
    a += 1
    ax = Instruction(vm.fetch()).ax()
    vm.get_const(ax)
    vm.replace(a)


# R(A) := (bool)B; if (C) pc++
def loadbool(inst, vm):
    a, b, c = inst.a_b_c()
    vm.push_boolean(b != 0)
    vm.replace(a+1)

    if c != 0:
        vm.add_pc(1)


# R(A), R(A+1) ... R(A+B) := nil
def loadnil(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1

    vm.push_nil()
    for i in range(a, a+b):
        vm.copy(-1, i)
    vm.pop(1)


# arith
def arith_binary(inst, vm, op):
    a, b, c = inst.a_b_c()
    a += 1

    vm.get_rk(b)
    vm.get_rk(c)
    vm.arith(op)
    vm.replace(a)


def arith_unary(inst, vm, op):
    a, b, _ = inst.a_b_c()
    a += 1
    b += 1

    vm.push_value(b)
    vm.arith(op)
    vm.replace(a)


def add(inst, vm):
    arith_binary(inst, vm, ArithOp.ADD)


def sub(inst, vm):
    arith_binary(inst, vm, ArithOp.SUB)


def mul(inst, vm):
    arith_binary(inst, vm, ArithOp.MUL)


def mod(inst, vm):
    arith_binary(inst, vm, ArithOp.MOD)


def luapow(inst, vm):
    arith_binary(inst, vm, ArithOp.POW)


def div(inst, vm):
    arith_binary(inst, vm, ArithOp.DIV)


def idiv(inst, vm):
    arith_binary(inst, vm, ArithOp.IDIV)


def band(inst, vm):
    arith_binary(inst, vm, ArithOp.BAND)


def bor(inst, vm):
    arith_binary(inst, vm, ArithOp.BOR)


def bxor(inst, vm):
    arith_binary(inst, vm, ArithOp.BXOR)


def shl(inst, vm):
    arith_binary(inst, vm, ArithOp.SHL)


def shr(inst, vm):
    arith_binary(inst, vm, ArithOp.SHR)


def unm(inst, vm):
    arith_unary(inst, vm, ArithOp.UNM)


def bnot(inst, vm):
    arith_unary(inst, vm, ArithOp.BNOT)


# R(A) := length of R(B)
def length(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    b += 1

    vm.len(b)
    vm.replace(a)


def concat(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    b += 1
    c += 1
    n = c - b + 1

    vm.check_stack(n)
    for i in range(b, c+1):
        vm.push_value(i)

    vm.concat(n)
    vm.replace(a)


def jmp(inst, vm):
    a, sbx = inst.a_sbx()
    vm.add_pc(sbx)
    if a != 0:
        vm.close_upvalues(a)


def compare(inst, vm, op):
    a, b, c = inst.a_b_c()
    vm.get_rk(b)
    vm.get_rk(c)
    if vm.compare(-2, op, -1) != (a != 0):
        vm.add_pc(1)
    vm.pop(2)


def eq(inst, vm):
    compare(inst, vm, CmpOp.EQ)


def lt(inst, vm):
    compare(inst, vm, CmpOp.LT)


def le(inst, vm):
    compare(inst, vm, CmpOp.LE)


# R(A) := not R(B)
def luanot(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    b += 1

    vm.push_boolean(not vm.to_boolean(b))
    vm.replace(a)


# if not (R(A) <=> C) then pc++
def test(inst, vm):
    a, _, c = inst.a_b_c()
    a += 1
    if vm.to_boolean(a) != (c != 0):
        vm.add_pc(1)


# if (R(B) <=> C) then R(A) := R(B) else pc++
def testset(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    b += 1

    if vm.to_boolean(b) != (c != 0):
        vm.copy(b, a)
    else:
        vm.add_pc(1)


# R(A) += R(A+2)
# if R(A) <?= R(A+1) then {
#   pc += sBx;
#   R(A+3) = R(A)
# }
def forloop(inst, vm):
    a, sbx = inst.a_sbx()
    a += 1

    vm.push_value(a+2)
    vm.push_value(a)
    vm.arith(ArithOp.ADD)
    vm.replace(a)

    positive_step = vm.to_number(a+2) >= 0
    if (positive_step and vm.compare(a, CmpOp.LE, a+1)) \
            or ((not positive_step) and vm.compare(a+1, CmpOp.LE, a)):
        vm.add_pc(sbx)
        vm.copy(a, a+3)


# R(A)-=R(A+2); pc+=sBx
def forprep(inst, vm):
    a, sbx = inst.a_sbx()
    a += 1

    if vm.type(a) == LuaType.STRING:
        vm.push_number(vm.to_number(a))
        vm.replace(a)

    if vm.type(a+1) == LuaType.STRING:
        vm.push_number(vm.to_number(a+1))
        vm.replace(a+1)

    if vm.type(a+2) == LuaType.STRING:
        vm.push_number(vm.to_number(a+2))
        vm.replace(a+2)

    vm.push_value(a)
    vm.push_value(a+2)
    vm.arith(ArithOp.SUB)
    vm.replace(a)
    vm.add_pc(sbx)


# R(A) := {}
def newtable(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    vm.create_table(LuaValue.fb2int(b), LuaValue.fb2int(c))
    vm.replace(a)


# R(A) := R(B)[RK(C)]
def gettable(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    b += 1
    vm.get_rk(c)
    vm.get_table(b)
    vm.replace(a)


# R(A)[R(B)] := RK(C)
def settable(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    vm.get_rk(b)
    vm.get_rk(c)
    vm.set_table(a)


# R(A)[(C-1)*LFIELDS_PER_FLUSH+i] := R(A+i), 1 <= i <= B
def setlist(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    c = c - 1 if c > 0 else inst.ax(vm.fetch())

    is_zero = b == 0
    if is_zero:
        b = vm.to_integer(-1) - a - 1
        vm.pop(1)

    vm.check_stack(1)
    idx = c * LFIELDS_PER_FLUSH
    for i in range(1, b+1):
        idx += 1
        vm.push_value(a+i)
        vm.set_i(a, idx)

    if is_zero:
        for i in range(vm.register_count() + 1, vm.get_top()+1):
            idx += 1
            vm.push_value(i)
            vm.set_i(a, idx)
        vm.set_top(vm.register_count())


# R(A+1) := R(B); R(A) := R(B)[RK(C)]
def luaself(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    b += 1
    vm.copy(b, a+1)
    vm.get_rk(c)
    vm.get_table(b)
    vm.replace(a)


# R(A) := closure(KPROTO[Bx])
def closure(inst, vm):
    a, bx = inst.a_bx()
    a += 1
    vm.load_proto(bx)
    vm.replace(a)


def vararg(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    if b != 1:
        vm.load_vararg(b-1)
        pop_results(a, b, vm)


def tailcall(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    c = 0
    nargs = push_func_and_args(a, b, vm)
    vm.call(nargs, c-1)
    pop_results(a, c, vm)


def call(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    nargs = push_func_and_args(a, b, vm)
    vm.call(nargs, c-1)
    pop_results(a, c, vm)


def luaret(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    if b == 1:
        pass
    elif b > 1:
        vm.check_stack(b-1)
        for i in range(a, a+b-1):
            vm.push_value(i)
    else:
        fix_stack(a, vm)


def push_func_and_args(a, b, vm):
    if b >= 1:
        vm.check_stack(b)
        for i in range(a, a+b):
            vm.push_value(i)
        return b-1
    else:
        fix_stack(a, vm)
        return vm.get_top() - vm.register_count() - 1


def fix_stack(a, vm):
    x = vm.to_integer(-1)
    vm.pop(1)

    vm.check_stack(x-a)
    for i in range(a, x):
        vm.push_value(i)
    vm.rotate(vm.register_count()+1, x-a)


def pop_results(a, c, vm):
    if c == 1:
        pass
    elif c > 1:
        for i in range(a+c-2, a-1, -1):
            vm.replace(i)
    else:
        vm.check_stack(1)
        vm.push_integer(a)


def lua_upvalue_index(idx):
    return Consts.LUA_REGISTRYINDEX - idx


def getupval(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    b += 1
    vm.copy(lua_upvalue_index(b), a)


def setupval(inst, vm):
    a, b, _ = inst.a_b_c()
    a += 1
    b += 1
    vm.copy(a, lua_upvalue_index(b))


def gettabup(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1
    b += 1

    vm.get_rk(c)
    vm.get_table(lua_upvalue_index(b))
    vm.replace(a)


def settabup(inst, vm):
    a, b, c = inst.a_b_c()
    a += 1

    vm.get_rk(b)
    vm.get_rk(c)
    vm.set_table(lua_upvalue_index(a))


op_codes = [
    #      T  A  B       C       mode   name        action
    OpCode(0, 1, OpArgR, OpArgN, IABC,  "MOVE    ", move),      # R(A) := R(B)
    OpCode(0, 1, OpArgK, OpArgN, IABx,  "LOADK   ", loadk),     # R(A) := Kst(Bx)
    OpCode(0, 1, OpArgN, OpArgN, IABx,  "LOADKX  ", loadkx),    # R(A) := Kst(extra arg)
    OpCode(0, 1, OpArgU, OpArgU, IABC,  "LOADBOOL", loadbool),  # R(A) := (bool)B; if (C) pc++
    OpCode(0, 1, OpArgU, OpArgN, IABC,  "LOADNIL ", loadnil),   # R(A), R(A+1), ..., R(A+B) := nil
    OpCode(0, 1, OpArgU, OpArgN, IABC,  "GETUPVAL", getupval),  # R(A) := UpValue[B]
    OpCode(0, 1, OpArgU, OpArgK, IABC,  "GETTABUP", gettabup),  # R(A) := UpValue[B][RK(C)]
    OpCode(0, 1, OpArgR, OpArgK, IABC,  "GETTABLE", gettable),  # R(A) := R(B)[RK(C)]
    OpCode(0, 0, OpArgK, OpArgK, IABC,  "SETTABUP", settabup),  # UpValue[A][RK(B)] := RK(C)
    OpCode(0, 0, OpArgU, OpArgN, IABC,  "SETUPVAL", setupval),  # UpValue[B] := R(A)
    OpCode(0, 0, OpArgK, OpArgK, IABC,  "SETTABLE", settable),  # R(A)[RK(B)] := RK(C)
    OpCode(0, 1, OpArgU, OpArgU, IABC,  "NEWTABLE", newtable),  # R(A) := {} (size = B,C)
    OpCode(0, 1, OpArgR, OpArgK, IABC,  "SELF    ", luaself),   # R(A+1) := R(B); R(A) := R(B)[RK(C)]
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "ADD     ", add),       # R(A) := RK(B) + RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "SUB     ", sub),       # R(A) := RK(B) - RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "MUL     ", mul),       # R(A) := RK(B) * RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "MOD     ", mod),       # R(A) := RK(B) % RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "POW     ", luapow),    # R(A) := RK(B) ^ RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "DIV     ", div),       # R(A) := RK(B) / RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "IDIV    ", idiv),      # R(A) := RK(B) // RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "BAND    ", band),      # R(A) := RK(B) & RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "BOR     ", bor),       # R(A) := RK(B) | RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "BXOR    ", bxor),      # R(A) := RK(B) ~ RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "SHL     ", shl),       # R(A) := RK(B) << RK(C)
    OpCode(0, 1, OpArgK, OpArgK, IABC,  "SHR     ", shr),       # R(A) := RK(B) >> RK(C)
    OpCode(0, 1, OpArgR, OpArgN, IABC,  "UNM     ", unm),       # R(A) := -R(B)
    OpCode(0, 1, OpArgR, OpArgN, IABC,  "BNOT    ", bnot),      # R(A) := ~R(B)
    OpCode(0, 1, OpArgR, OpArgN, IABC,  "NOT     ", luanot),    # R(A) := not R(B)
    OpCode(0, 1, OpArgR, OpArgN, IABC,  "LEN     ", length),    # R(A) := length of R(B)
    OpCode(0, 1, OpArgR, OpArgR, IABC,  "CONCAT  ", concat),    # R(A) := R(B).. ... ..R(C)
    OpCode(0, 0, OpArgR, OpArgN, IAsBx, "JMP     ", jmp),       # pc+=sBx; if (A) close all upvalues >= R(A - 1)
    OpCode(1, 0, OpArgK, OpArgK, IABC,  "EQ      ", eq),        # if ((RK(B) == RK(C)) ~= A) then pc++
    OpCode(1, 0, OpArgK, OpArgK, IABC,  "LT      ", lt),        # if ((RK(B) <  RK(C)) ~= A) then pc++
    OpCode(1, 0, OpArgK, OpArgK, IABC,  "LE      ", le),        # if ((RK(B) <= RK(C)) ~= A) then pc++
    OpCode(1, 0, OpArgN, OpArgU, IABC,  "TEST    ", test),      # if not (R(A) <=> C) then pc++
    OpCode(1, 1, OpArgR, OpArgU, IABC,  "TESTSET ", testset),   # if (R(B) <=> C) then R(A) := R(B) else pc++
    OpCode(0, 1, OpArgU, OpArgU, IABC,  "CALL    ", call),      # R(A), ...,R(A+C-2) := R(A)(R(A+1), ...,R(A+B-1))
    OpCode(0, 1, OpArgU, OpArgU, IABC,  "TAILCALL", tailcall),  # return R(A)(R(A+1), ... ,R(A+B-1))
    OpCode(0, 0, OpArgU, OpArgN, IABC,  "RETURN  ", luaret),    # return R(A), ... ,R(A+B-2)
    OpCode(0, 1, OpArgR, OpArgN, IAsBx, "FORLOOP ", forloop),   # R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }
    OpCode(0, 1, OpArgR, OpArgN, IAsBx, "FORPREP ", forprep),   # R(A)-=R(A+2); pc+=sBx
    OpCode(0, 0, OpArgN, OpArgU, IABC,  "TFORCALL", None),      # R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));
    OpCode(0, 1, OpArgR, OpArgN, IAsBx, "TFORLOOP", None),      # if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx }
    OpCode(0, 0, OpArgU, OpArgU, IABC,  "SETLIST ", setlist),   # R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
    OpCode(0, 1, OpArgU, OpArgN, IABx,  "CLOSURE ", closure),   # R(A) := closure(KPROTO[Bx])
    OpCode(0, 1, OpArgU, OpArgN, IABC,  "VARARG  ", vararg),    # R(A), R(A+1), ..., R(A+B-2) = vararg
    OpCode(0, 0, OpArgU, OpArgU, IAx,   "EXTRAARG", None),      # extra (larger) argument for previous opcode
]


class Instruction:
    MAXARG_Bx = (1 << 18) - 1
    MAXARG_sBx = MAXARG_Bx >> 1

    def __init__(self, code):
        self.code = code

    def op_code(self):
        return self.code & 0x3f

    def a_b_c(self):
        return (self.code >> 6) & 0xff, (self.code >> 23) & 0x1ff, (self.code >> 14) & 0x1ff

    def a_bx(self):
        return (self.code >> 6) & 0xff, (self.code >> 14)

    def a_sbx(self):
        a, bx = self.a_bx()
        return a, bx - Instruction.MAXARG_sBx

    def ax(self):
        return self.code >> 6

    def op_name(self):
        return op_codes[self.op_code()].name

    def op_mode(self):
        return op_codes[self.op_code()].op_mode

    def arg_b_mode(self):
        return op_codes[self.op_code()].arg_b_mode

    def arg_c_mode(self):
        return op_codes[self.op_code()].arg_c_mode

    def execute(self, vm):
        op_codes[self.op_code()].action(self, vm)

    def dump(self):
        print(self.op_name(), end=' ')
        if self.op_mode() == IABC:
            a, b, c = self.a_b_c()
            print('%8d' % a, end='')
            if self.arg_b_mode() != OpArgN:
                if b > 0xff:
                    print(' %8d' % (-1 - (b & 0xff)), end='')
                else:
                    print(' %8d' % b, end='')
            if self.arg_c_mode() != OpArgN:
                if c > 0xff:
                    print(' %8d' % (-1 - (c & 0xff)), end='')
                else:
                    print(' %8d' % c, end='')
        elif self.op_mode() == IABx:
            a, bx = self.a_bx()
            print('%8d' % a, end='')
            if self.arg_b_mode() == OpArgK:
                print(' %8d' % (-1 - bx), end='')
            elif self.arg_b_mode() == OpArgU:
                print(' %8d' % bx, end='')
        elif self.op_mode() == IAsBx:
            a, sbx = self.a_sbx()
            print('%8d %8d' % (a, sbx), end='')
        elif self.op_mode() == IAx:
            ax = self.ax()
            print('%8d' % (-1 - ax), end='')
        print()
