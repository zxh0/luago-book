from vm.lua_opcode import OpCode
from compiler.lua_token import TokenKind
from vm.lua_opcode import Instruction
from vm.lua_value import LuaValue
from vm.prototype import Prototype
from vm.upvalue import Upvalue


class LocalVarInfo:
    def __init__(self, name, prev, scope_level, slot):
        self.name = name
        self.prev = prev
        self.scope_level = scope_level
        self.slot = slot
        self.captured = False


class UpvalInfo:
    def __init__(self, slot, upval_index, index):
        self.local_var_slot = slot
        self.upval_index = upval_index
        self.index = index


class FuncInfo:
    arith_and_bitwise_binops = {
        TokenKind.OP_ADD: OpCode.ADD,
        TokenKind.OP_SUB: OpCode.SUB,
        TokenKind.OP_MUL: OpCode.MUL,
        TokenKind.OP_MOD: OpCode.MOD,
        TokenKind.OP_POW: OpCode.POW,
        TokenKind.OP_DIV: OpCode.DIV,
        TokenKind.OP_IDIV: OpCode.IDIV,
        TokenKind.OP_BAND: OpCode.BAND,
        TokenKind.OP_BOR: OpCode.BOR,
        TokenKind.OP_BXOR: OpCode.BXOR,
        TokenKind.OP_SHL: OpCode.SHL,
        TokenKind.OP_SHR: OpCode.SHR,
    }

    def __init__(self, parent, fd):
        self.parent = parent
        self.sub_funcs = []
        self.used_regs = 0
        self.max_regs = 0
        self.scope_level = 0
        self.local_vars = []
        self.local_names = {}
        self.upvalues = {}
        self.constants = {}
        self.breaks = [None]
        self.insts = []
        self.num_params = 0 if fd.par_list is None else len(fd.par_list)
        self.is_vararg = fd.is_var_arg

    def index_of_constant(self, k):
        if k in self.constants:
            return self.constants[k]

        idx = len(self.constants)
        self.constants[k] = idx
        return idx

    def alloc_reg(self):
        self.used_regs += 1
        if self.used_regs >= 255:
            raise Exception('function or expression needs too many registers')

        if self.used_regs > self.max_regs:
            self.max_regs = self.used_regs

        return self.used_regs - 1

    def free_reg(self):
        self.used_regs -= 1

    def alloc_regs(self, n):
        for i in range(n):
            self.alloc_reg()
        return self.used_regs - n

    def free_regs(self, n):
        for i in range(n):
            self.free_reg()

    def enter_scope(self, break_able):
        self.scope_level += 1
        if break_able:
            self.breaks.append([])
        else:
            self.breaks.append(None)

    def exit_scope(self):
        if len(self.breaks) > 0:
            pending_break_jmps = self.breaks[-1]
            self.breaks = self.breaks[:len(self.breaks)-1]
            if pending_break_jmps:
                a = self.get_jmp_arg_a()
                for pc in pending_break_jmps:
                    sbx = self.pc() - pc
                    i = ((sbx + Instruction.MAXARG_sBx) << 14) | (a << 6) | OpCode.JMP
                    self.insts[pc] = i

        self.scope_level -= 1
        for k in list(self.local_names):
            local_var = self.local_names[k]
            if local_var.scope_level > self.scope_level:
                self.remove_local_var(local_var)

    def add_local_var(self, name):
        prev = None
        if name in self.local_names:
            prev = self.local_names[name]
        new_var = LocalVarInfo(name, prev, self.scope_level, self.alloc_reg())

        self.local_vars.append(new_var)
        self.local_names[name] = new_var
        return new_var.slot

    def remove_local_var(self, local_var):
        self.free_reg()
        if local_var.prev is None:
            self.local_names.pop(local_var.name)
        elif local_var.prev.scope_level == local_var.scope_level:
            self.remove_local_var(local_var.prev)
        else:
            self.local_names[local_var.name] = local_var.prev

    def slot_of_local_var(self, name):
        if name in self.local_names:
            return self.local_names[name].slot
        return -1

    def add_break_jmp(self, pc):
        for i in range(self.scope_level, -1, -1):
            if self.breaks[i] is not None:
                self.breaks[i].append(pc)
                return

        raise Exception('<break> not inside a loop!')

    def index_of_upval(self, name):
        if name in self.upvalues:
            return self.upvalues[name].index

        if self.parent is not None:
            if name in self.parent.local_names:
                idx = len(self.upvalues)
                local_var = self.parent.local_names[name]
                self.upvalues[name] = UpvalInfo(local_var.slot, -1, idx)
                local_var.captured = True
                return idx

            upval_index = self.parent.index_of_upval(name)
            if upval_index >= 0:
                idx = len(self.upvalues)
                self.upvalues[name] = UpvalInfo(-1, upval_index, idx)
                return idx

        return -1

    def close_open_upvals(self):
        a = self.get_jmp_arg_a()
        if a > 0:
            self.emit_jmp(a, 0)

    def get_jmp_arg_a(self):
        has_captured_local_vars = False
        min_slot_of_local_vars = self.max_regs
        for local_var in self.local_names.values():
            if local_var.scope_level == self.scope_level:
                v = local_var
                while v is not None and v.scope_level == self.scope_level:
                    if v.captured:
                        has_captured_local_vars = True
                    if v.slot < min_slot_of_local_vars and v.name[0] == '(':
                        min_slot_of_local_vars = v.slot
                    v = v.prev

        if has_captured_local_vars:
            return min_slot_of_local_vars + 1

        return 0

    def pc(self):
        return len(self.insts) - 1

    def fix_sbx(self, pc, sbx):
        i = self.insts[pc]
        i = ((i << 18) & 0xffffffff) >> 18
        i = (i | (sbx + Instruction.MAXARG_sBx) << 14) & 0xffffffff
        self.insts[pc] = i

    def fix_end_pc(self, name, delta):
        for i in range(len(self.local_vars), -1, -1):
            local_var = self.local_vars[i]
            if local_var.name == name:
                local_var.end_pc += delta
                return

    def emit_abc(self, opcode, a, b, c):
        # print("%5s %8d %8d %8d %8d" % ('ABC', opcode, a, b, c))
        i = ((b << 23) | (c << 14) | (a << 6) | opcode) & 0xffffffff
        self.insts.append(i)

    def emit_a_bx(self, opcode, a, bx):
        # print("%5s %8d %8d %8d" % ('ABx', opcode, a, bx))
        i = ((bx << 14) | (a << 6) | opcode) & 0xffffffff
        self.insts.append(i)

    def emit_as_bx(self, opcode, a, sbx):
        # print("%5s %8d %8d %8d" % ('AsBx', opcode, a, sbx))
        i = (((sbx + Instruction.MAXARG_sBx) << 14) | (a << 6) | opcode) & 0xffffffff
        self.insts.append(i)

    def emit_ax(self, opcode, ax):
        # print("%5s %8d %8d" % ('AX', opcode, ax))
        i = ((ax << 6) | opcode) & 0xffffffff
        self.insts.append(i)

    def emit_move(self, a, b):
        self.emit_abc(OpCode.MOVE, a, b, 0)

    def emit_load_nil(self, a, n):
        self.emit_abc(OpCode.LOADNIL, a, n-1, 0)

    def emit_load_bool(self, a, b, c):
        self.emit_abc(OpCode.LOADBOOL, a, b, c)

    def emit_load_k(self, a, k):
        idx = self.index_of_constant(k)
        if idx < (1 << 18):
            self.emit_a_bx(OpCode.LOADK, a, idx)
        else:
            self.emit_a_bx(OpCode.LOADKX, a, 0)
            self.emit_ax(OpCode.EXTRAARG, idx)

    def emit_vararg(self, a, n):
        self.emit_abc(OpCode.VARARG, a, n+1, 0)

    def emit_closure(self, a, bx):
        self.emit_a_bx(OpCode.CLOSURE, a, bx)

    def emit_new_table(self, a, narr, nrec):
        self.emit_abc(OpCode.NEWTABLE, a, LuaValue.int2fb(narr), LuaValue.int2fb(nrec))

    def emit_set_list(self, a, b, c):
        self.emit_abc(OpCode.SETLIST, a, b, c)

    def emit_get_table(self, a, b, c):
        self.emit_abc(OpCode.GETTABLE, a, b, c)

    def emit_set_table(self, a, b, c):
        self.emit_abc(OpCode.SETTABLE, a, b, c)

    def emit_get_upval(self, a, b):
        self.emit_abc(OpCode.GETUPVAL, a, b, 0)

    def emit_set_upval(self, a, b):
        self.emit_abc(OpCode.SETUPVAL, a, b, 0)

    def emit_get_tabup(self, a, b, c):
        self.emit_abc(OpCode.GETTABUP, a, b, c)

    def emit_set_tabup(self, a, b, c):
        self.emit_abc(OpCode.SETTABUP, a, b, c)

    def emit_call(self, a, nargs, nret):
        self.emit_abc(OpCode.CALL, a, nargs+1, nret+1)

    def emit_tail_call(self, a, nargs):
        self.emit_abc(OpCode.TAILCALL, a, nargs+1, 0)

    def emit_return(self, a, n):
        self.emit_abc(OpCode.RETURN, a, n+1, 0)

    def emit_self(self, a, b, c):
        self.emit_abc(OpCode.SELF, a, b, c)

    def emit_jmp(self, a, sbx):
        self.emit_as_bx(OpCode.JMP, a, sbx)
        return len(self.insts) - 1

    def emit_test(self, a, c):
        self.emit_abc(OpCode.TEST, a, 0, c)

    def emit_test_set(self, a, b, c):
        self.emit_abc(OpCode.TESTSET, a, b, c)

    def emit_for_prep(self, a, sbx):
        self.emit_as_bx(OpCode.FORPREP, a, sbx)
        return len(self.insts) - 1

    def emit_for_loop(self, a, sbx):
        self.emit_as_bx(OpCode.FORLOOP, a, sbx)
        return len(self.insts) - 1

    def emit_tfor_call(self, a, c):
        self.emit_abc(OpCode.TFORCALL, a, 0, c)

    def emit_tfor_loop(self, a, sbx):
        self.emit_as_bx(OpCode.TFORLOOP, a, sbx)

    def emit_unary_op(self, op, a, b):
        if op == TokenKind.OP_NOT:
            self.emit_abc(OpCode.NOT, a, b, 0)
        elif op == TokenKind.OP_BNOT:
            self.emit_abc(OpCode.BNOT, a, b, 0)
        elif op == TokenKind.OP_LEN:
            self.emit_abc(OpCode.LEN, a, b, 0)
        elif op == TokenKind.OP_UNM:
            self.emit_abc(OpCode.UNM, a, b, 0)

    def emit_binary_op(self, op, a, b, c):
        if op in FuncInfo.arith_and_bitwise_binops:
            self.emit_abc(FuncInfo.arith_and_bitwise_binops[op], a, b, c)
        else:
            if op == TokenKind.OP_EQ:
                self.emit_abc(OpCode.EQ, 1, b, c)
            elif op == TokenKind.OP_NE:
                self.emit_abc(OpCode.EQ, 0, b, c)
            elif op == TokenKind.OP_LT:
                self.emit_abc(OpCode.LT, 1, b, c)
            elif op == TokenKind.OP_GT:
                self.emit_abc(OpCode.LT, 1, c, b)
            elif op == TokenKind.OP_LE:
                self.emit_abc(OpCode.LE, 1, b, c)
            elif op == TokenKind.OP_GE:
                self.emit_abc(OpCode.LE, 1, c, b)

            self.emit_jmp(0, 1)
            self.emit_load_bool(a, 0, 1)
            self.emit_load_bool(a, 1, 0)

    def to_proto(self):
        proto = Prototype()
        proto.num_params = self.num_params
        proto.max_stack_size = self.max_regs
        proto.code = self.insts
        proto.constants = self.get_constants()
        proto.upvalues = self.get_upvalues()
        proto.protos = []
        proto.line_infos = []
        proto.local_vars = self.local_vars
        proto.upvalue_names = self.get_upvalue_names()

        for fi in self.sub_funcs:
            proto.protos.append(fi.to_proto())

        if proto.get_max_stack_size() < 2:
            proto.max_stack_size = 2
        if self.is_vararg:
            proto.is_vararg = 1
        return proto

    def get_upvalues(self):
        upvals = [None for _ in range(len(self.upvalues))]
        for _, upval_info in self.upvalues.items():
            if upval_info.local_var_slot >= 0:
                upval = Upvalue(True, upval_info.local_var_slot)
            else:
                upval = Upvalue(False, upval_info.upval_index)
            upvals[upval_info.index] = upval
        return upvals

    def get_upvalue_names(self):
        names = ['' for _ in range(len(self.upvalues))]
        for name, upval_info in self.upvalues.items():
            names[upval_info.index] = name

        return names

    def get_constants(self):
        consts = {}
        for k, idx in self.constants.items():
            consts[idx] = k
        return consts
