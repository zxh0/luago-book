from lua_stack import LuaStack
from lua_type import LuaType
from lua_value import LuaValue
from arith_op import ArithOp
from arithmetic import Arithmetic
from cmp_op import CmpOp
from compare import Compare
from lua_table import LuaTable


class LuaState:
    def __init__(self, proto):
        self.stack = LuaStack()
        self.proto = proto
        self.pc = 0

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
        assert(result is not None)
        self.stack.push(result)

    def len(self, idx):
        val = self.stack.get(idx)
        assert(val is not None)
        if isinstance(val, str) or isinstance(val, LuaTable):
            self.stack.push(len(val))
        else:
            raise Exception('length error')

    def concat(self, n):
        if n == 0:
            self.stack.push('')
        elif n >= 2:
            for i in range(1, n):
                assert(self.is_string(-1) and self.is_string(-2))
                s2 = self.to_string(-1)
                s1 = self.to_string(-2)
                self.stack.pop()
                self.stack.pop()
                self.stack.push(s1+s2)

    def compare(self, idx1, op, idx2):
        if not self.stack.is_valid(idx1) or not self.stack.is_valid(idx2):
            return False

        a = self.stack.get(idx1)
        b = self.stack.get(idx2)

        if op == CmpOp.EQ:
            return Compare.eq(a, b)
        elif op == CmpOp.LT:
            return Compare.lt(a, b)
        elif op == CmpOp.LE:
            return Compare.le(a, b)

    # vm
    def get_pc(self):
        return self.pc

    def add_pc(self, n):
        self.pc += n

    def fetch(self):
        code = self.proto.get_code()[self.pc]
        self.pc += 1
        return code

    def get_const(self, idx):
        self.stack.push(self.proto.get_constants()[idx])

    def get_rk(self, rk):
        if rk > 0xff:   # constant
            self.get_const(rk & 0xff)
        else:           # register
            self.push_value(rk + 1)

    # table
    def create_table(self, narr, nrec):
        table = LuaTable(narr, nrec)
        self.stack.push(table)

    @staticmethod
    def get_table_val(t, k):
        if isinstance(t, LuaTable):
            return t.get(k)
        raise Exception('not a table')

    def get_table(self, idx):
        t = self.stack.get(idx)
        k = self.stack.pop()
        v = LuaState.get_table_val(t, k)
        self.stack.push(v)
        return LuaValue.type_of(v)

    def get_i(self, idx, i):
        t = self.stack.get(idx)
        v = LuaState.get_table_val(t, i)
        self.stack.push(v)
        return LuaValue.type_of(v)

    def set_table(self, idx):
        t = self.stack.get(idx)
        v = self.stack.pop()
        k = self.stack.pop()
        LuaState.set_table_kv(t, k, v)

    @staticmethod
    def set_table_kv(t, k, v):
        if isinstance(t, LuaTable):
            t.put(k, v)
            return
        raise Exception('not a table')

    def set_field(self, idx, k):
        t = self.stack.get(idx)
        v = self.stack.pop()
        LuaState.set_table_kv(t, k, v)

    def set_i(self, idx, i):
        t = self.stack.get(idx)
        v = self.stack.pop()
        LuaState.set_table_kv(t, i, v)