from lua_stack import LuaStack
from lua_type import LuaType
from lua_value import LuaValue
from arith_op import ArithOp
from arithmetic import Arithmetic
from cmp_op import CmpOp
from compare import Compare
from lua_table import LuaTable
from binary_chunk import BinaryChunk
from closure import Closure
from lua_opcode import Instruction
from lua_opcode import OpCode
from thread_state import ThreadStatus
from consts import Consts


class LuaState:
    def __init__(self):
        self.stack = LuaStack(self)
        self.registry = LuaTable(0, 0)
        self.registry.put(Consts.LUA_RIDX_GLOBALS, LuaTable(0, 0))
        self.push_lua_stack(LuaStack(Consts.LUA_MIN_STACK))
        self.time = 0

    def get_top(self):
        return self.stack.top()

    def abs_index(self, idx):
        return self.stack.abs_index(idx)

    def check_stack(self, n):
        return self.stack.check(n)

    def pop(self, n):
        for i in range(n):
            self.stack.pop()

    def copy(self, src, dst):
        self.stack.set(dst, self.stack.get(src))

    def push_value(self, idx):
        self.stack.push(self.stack.get(idx))

    def replace(self, idx):
        self.stack.set(idx, self.stack.pop())

    def insert(self, idx):
        self.rotate(idx, 1)

    def remove(self, idx):
        self.rotate(idx, -1)
        self.pop(1)

    def rotate(self, idx, n):
        t = self.stack.top() - 1
        p = self.stack.abs_index(idx) - 1
        m = t - n if n >= 0 else p - n - 1
        self.stack.reverse(p, m)
        self.stack.reverse(m+1, t)
        self.stack.reverse(p, t)

    def set_top(self, idx):
        new_top = self.stack.abs_index(idx)
        assert(new_top >= 0)

        n = self.stack.top() - new_top
        if n > 0:
            for i in range(n):
                self.stack.pop()
        elif n < 0:
            for i in range(-n):
                self.stack.push(None)

    @staticmethod
    def type_name(tp):
        if tp == LuaType.NONE:
            return 'no value'
        elif tp == LuaType.NIL:
            return 'nil'
        elif tp == LuaType.BOOLEAN:
            return 'boolean'
        elif tp == LuaType.NUMBER:
            return 'number'
        elif tp == LuaType.STRING:
            return 'string'
        elif tp == LuaType.TABLE:
            return 'table'
        elif tp == LuaType.FUNCTION:
            return 'function'
        elif tp == LuaType.THREAD:
            return 'thread'
        elif tp == LuaType.USER_DATA:
            return 'userdata'

    def type(self, idx):
        if self.stack.is_valid(idx):
            return LuaValue.type_of(self.stack.get(idx))
        return LuaType.NONE

    def is_none(self, idx):
        return self.type(idx) == LuaType.NONE

    def is_nil(self, idx):
        return self.type(idx) == LuaType.NIL

    def is_none_or_nil(self, idx):
        return self.is_none(idx) or self.is_nil(idx)

    def is_boolean(self, idx):
        return self.type(idx) == LuaType.BOOLEAN

    def is_integer(self, idx):
        return isinstance(self.stack.get(idx), int)

    def is_number(self, idx):
        return self.to_number(idx) is not None

    def is_string(self, idx):
        tp = self.type(idx)
        return tp == LuaType.STRING or tp == LuaType.NUMBER

    def is_table(self, idx):
        return self.type(idx) == LuaType.TABLE

    def is_thread(self, idx):
        return self.type(idx) == LuaType.THREAD

    def is_function(self, idx):
        return self.type(idx) == LuaType.FUNCTION

    def to_boolean(self, idx):
        return LuaValue.to_boolean(self.stack.get(idx))

    def to_integer(self, idx):
        i = self.to_integerx(idx)
        return 0 if i is None else i

    def to_integerx(self, idx):
        val = self.stack.get(idx)
        return val if isinstance(val, int) else None

    def to_number(self, idx):
        val = self.stack.get(idx)
        if isinstance(val, float):
            return val
        elif isinstance(val, int):
            return float(val)
        return 0

    def to_string(self, idx):
        val = self.stack.get(idx)
        if isinstance(val, str):
            return val
        elif isinstance(val, int) or isinstance(val, float):
            s = str(val)
            self.stack.set(idx, s)
            return s
        return ''

    def push_nil(self):
        self.stack.push(None)

    def push_boolean(self, b):
        self.stack.push(b)

    def push_integer(self, n):
        self.stack.push(n)

    def push_number(self, n):
        self.stack.push(n)

    def push_string(self, s):
        self.stack.push(s)

    def print_stack(self):
        top = self.stack.top()
        for i in range(1, top+1):
            t = self.type(i)
            if t == LuaType.BOOLEAN:
                print('[%s]' % ('true' if self.to_boolean(i) else 'false'), end='')
            elif t == LuaType.NUMBER:
                if self.is_integer(i):
                    print('[%d]' % self.to_integer(i), end='')
                else:
                    print('[%g]' % self.to_number(i), end='')
            elif t == LuaType.STRING:
                print('["%s"]' % self.to_string(i), end='')
            else:
                print('[%s]' % LuaState.type_name(t), end='')

        print()

    def arith(self, op):
        b = self.stack.pop()
        a = self.stack.pop() if (op != ArithOp.UNM and op != ArithOp.BNOT) else b
        result = Arithmetic.arith(a, op, b)
        if result is None:
            name = Arithmetic.operators[op].metamethod
            metamethod = self.get_metamethod(a, b, name)
            result = self.call_metamethod(a, metamethod, b) if metamethod else None

        assert(result is not None)
        self.stack.push(result)

    def len(self, idx):
        val = self.stack.get(idx)
        assert(val is not None)
        if isinstance(val, str):
            self.stack.push(len(val))
            return

        metamethod = self.get_metamethod(val, val, '__len')
        if metamethod is not None:
            self.stack.push(self.call_metamethod(val, metamethod, val))
            return

        if isinstance(val, LuaTable):
            self.stack.push(len(val))
            return
        raise Exception('length error')

    def concat(self, n):
        if n == 0:
            self.stack.push('')
        elif n >= 2:
            for i in range(1, n):
                if self.is_string(-1) and self.is_string(-2):
                    s2 = self.to_string(-1)
                    s1 = self.to_string(-2)
                    self.stack.pop()
                    self.stack.pop()
                    self.stack.push(s1+s2)
                    continue

                b = self.stack.pop()
                a = self.stack.pop()
                mm = self.get_metamethod(a, b, '__concat')
                if mm:
                    self.stack.push(self.call_metamethod(a, mm, b))
                    continue

                raise Exception('concatenation error!')

    def compare(self, idx1, op, idx2):
        if not self.stack.is_valid(idx1) or not self.stack.is_valid(idx2):
            return False

        a = self.stack.get(idx1)
        b = self.stack.get(idx2)

        if op == CmpOp.EQ:
            return Compare.eq(a, b, self)
        elif op == CmpOp.LT:
            return Compare.lt(a, b, self)
        elif op == CmpOp.LE:
            return Compare.le(a, b, self)

    # vm
    def get_pc(self):
        return self.stack.pc

    def add_pc(self, n):
        self.stack.pc += n

    def fetch(self):
        code = self.stack.closure.proto.get_code()[self.stack.pc]
        self.stack.pc += 1
        return code

    def get_const(self, idx):
        self.stack.push(self.stack.closure.proto.get_constants()[idx])

    def get_rk(self, rk):
        if rk > 0xff:   # constant
            self.get_const(rk & 0xff)
        else:           # register
            self.push_value(rk + 1)

    # table
    def create_table(self, narr, nrec):
        table = LuaTable(narr, nrec)
        self.stack.push(table)

    def get_table_val(self, t, k, raw):
        if isinstance(t, LuaTable):
            v = t.get(k)
            if raw or (v is not None) or (not t.has_metafield('__index')):
                return v
        if not raw:
            mf = self.get_metafield(t, '__index')
            if mf:
                if isinstance(mf, LuaTable):
                    return self.get_table_val(mf, k, False)
                elif isinstance(mf, Closure):
                    v = self.call_metamethod(t, mf, k)
                    return v
        raise Exception('not a table')

    def get_table(self, idx):
        t = self.stack.get(idx)
        k = self.stack.pop()
        v = self.get_table_val(t, k, False)
        self.stack.push(v)
        return LuaValue.type_of(v)

    def get_i(self, idx, i):
        t = self.stack.get(idx)
        v = self.get_table_val(t, i, False)
        self.stack.push(v)
        return LuaValue.type_of(v)

    def set_table(self, idx):
        t = self.stack.get(idx)
        v = self.stack.pop()
        k = self.stack.pop()
        self.set_table_kv(t, k, v, False)

    def set_table_kv(self, t, k, v, raw):
        if isinstance(t, LuaTable):
            if raw or t.get(k) or not t.has_metafield('__newindex'):
                t.put(k, v)
                return
        if not raw:
            mf = self.get_metafield(t, '__newindex')
            if mf:
                if isinstance(mf, LuaTable):
                    self.set_table_kv(mf, k, v, False)
                    return
                if isinstance(mf, Closure):
                    self.stack.push(mf)
                    self.stack.push(t)
                    self.stack.push(k)
                    self.stack.push(v)
                    self.call(3, 0)
                    return
        raise Exception('not a table')

    def set_field(self, idx, k):
        t = self.stack.get(idx)
        v = self.stack.pop()
        self.set_table_kv(t, k, v, False)

    def set_i(self, idx, i):
        t = self.stack.get(idx)
        v = self.stack.pop()
        self.set_table_kv(t, i, v, False)

    def load(self, chunk):
        bc = BinaryChunk(chunk)
        proto = bc.undump()
        closure = Closure(proto, None, 0)
        self.stack.push(closure)

        if len(proto.upvalues) > 0:
            env = self.registry.get(Consts.LUA_RIDX_GLOBALS)
            closure.upvals[0] = env
            print('env: ', end='')
            env.dump()
        return ThreadStatus.OK

    def call(self, nargs, nresults):
        val = self.stack.get(-(nargs+1))
        f = val if isinstance(val, Closure) else None
        if f is None:
            metamethod = self.get_metafield(val, '__call')
            if metamethod and isinstance(metamethod, Closure):
                self.stack.push(val)
                self.insert(-(nargs+2))
                nargs += 1
                f = metamethod

        if f:
            if f.proto:
                self.call_lua_closure(nargs, nresults, f)
            else:
                self.call_py_closure(nargs, nresults, f)
        else:
            raise Exception(f, 'is not a function')

    def call_lua_closure(self, nargs, nresults, c):
        nregs = c.proto.get_max_stack_size()
        nparams = c.proto.get_num_params()
        is_vararg = c.proto.get_is_vararg()

        # create new lua stack
        new_stack = LuaStack(self)
        new_stack.closure = c

        # pass args, pop func
        func_and_args = self.stack.popn(nargs+1)
        new_stack.pushn(func_and_args[1:], nparams)
        if nargs > nparams and is_vararg:
            new_stack.varargs = func_and_args[nparams+1:]

        # run closure
        self.push_lua_stack(new_stack)
        self.set_top(nregs)
        self.run_lua_closure()
        self.pop_lua_stack()

        # return results
        if nresults != 0:
            results = new_stack.popn(new_stack.top() - nregs)
            self.stack.check(len(results))
            self.stack.pushn(results, nresults)

    def run_lua_closure(self):
        while True:
            # pc = self.get_pc() + 1
            inst = Instruction(self.fetch())
            inst.execute(self)
            # print('(%3d) [%02d] %-12s ' % (self.time, pc, inst.op_name()), end='')
            # self.print_stack()
            self.time += 1
            if inst.op_code() == OpCode.RETURN:
                break

    def push_lua_stack(self, s):
        s.caller = self.stack
        self.stack = s

    def pop_lua_stack(self):
        s = self.stack
        self.stack = s.caller
        s.caller = None

    def register_count(self):
        return self.stack.closure.proto.get_max_stack_size()

    def load_vararg(self, n):
        if n < 0:
            n = len(self.stack.varargs)

        self.stack.check(n)
        self.stack.pushn(self.stack.varargs, n)

    def load_proto(self, idx):
        proto = self.stack.closure.proto.get_protos()[idx]
        c = Closure(proto, None, 0)
        self.stack.push(c)

        for i in range(len(proto.upvalues)):
            upvalue = proto.get_upvalues()[i]
            idx = upvalue.get_idx()
            if upvalue.get_in_stack():
                if idx not in self.stack.open_upvalues:
                    self.stack.open_upvalues[idx] = self.stack.slots[idx]
                c.upvals[i] = self.stack.open_upvalues[idx]
            else:
                c.upvals[i] = self.stack.closure.upvals[idx]

    def push_py_function(self, func):
        py_closure = Closure(None, func, 0)
        self.stack.push(py_closure)

    def is_py_function(self, idx):
        val = self.stack.get(idx)
        return val and isinstance(val, Closure) and val.py_func is not None

    def to_py_function(self, idx):
        val = self.stack.get(idx)
        if val and isinstance(val, Closure) and val.py_func is not None:
            return val.py_func
        return None

    def call_py_closure(self, nargs, nresults, c):
        new_stack = LuaStack(self)
        new_stack.closure = c

        args = self.stack.popn(nargs)
        new_stack.pushn(args, nargs)
        self.stack.pop()

        self.push_lua_stack(new_stack)
        r = c.py_func(self)
        self.pop_lua_stack()

        if nresults != 0:
            results = new_stack.popn(r)
            self.stack.check(len(results))
            self.stack.pushn(results, nresults)

    def push_global_table(self):
        g = self.registry.get(Consts.LUA_RIDX_GLOBALS)
        self.stack.push(g)

    def get_global(self, name):
        t = self.registry.get(Consts.LUA_RIDX_GLOBALS)
        return self.get_table_val(t, name, False)

    def set_global(self, name):
        t = self.registry.get(Consts.LUA_RIDX_GLOBALS)
        v = self.stack.pop()
        self.set_table_kv(t, name, v, False)

    def register(self, name, func):
        self.push_py_function(func)
        self.set_global(name)

    def push_py_closure(self, py_func, n):
        closure = Closure(None, py_func, n)
        for i in range(n, 0, -1):
            v = self.stack.pop()
            closure.upvals[i-1] = v
        self.stack.push(closure)

    def close_upvalues(self, a):
        for k, v in self.stack.open_upvalues:
            if k >= a-1:
                v.migrate()
                self.stack.open_upvalues.pop(k)

    def set_metatable(self, idx):
        v = self.stack.get(idx)
        mt = self.stack.pop()
        if mt is None:
            self.set_metatable_kv(v, None)
        elif isinstance(mt, LuaTable):
            self.set_metatable_kv(v, mt)
        else:
            raise Exception('table expected!')

    def set_metatable_kv(self, val, mt):
        if isinstance(val, LuaTable):
            val.metatable = mt
        else:
            key = '_MT' + LuaValue.type_of(val)
            self.registry.put(key, mt)

    def get_metatable(self, idx):
        v = self.stack.get(idx)
        mt = self.get_metatable_k(v)
        if mt:
            self.stack.push(mt)
            return True
        else:
            return False

    def get_metatable_k(self, val):
        if isinstance(val, LuaTable):
            return val.metatable
        else:
            key = '_MT' + str(LuaValue.type_of(val))
            return self.registry.get(key)

    def call_metamethod(self, a, mm, b):
        self.stack.push(mm)
        self.stack.push(a)
        self.stack.push(b)
        self.call(2, 1)
        return self.stack.pop()

    def get_metafield(self, val, name):
        mt = self.get_metatable_k(val)
        if mt is not None:
            return mt.get(name)
        return None

    def get_metamethod(self, a, b, name):
        metamethod = self.get_metafield(a, name)
        if not metamethod:
            metamethod = self.get_metafield(b, name)
        return metamethod

