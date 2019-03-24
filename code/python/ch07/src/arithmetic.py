from collections import namedtuple
from lua_value import LuaValue
from arith_op import ArithOp


def add(a, b):
    return a + b


def sub(a, b):
    return a - b


def mul(a, b):
    return a * b


def mod(a, b):
    return a % b


def div(a, b):
    return a / b


def fdiv(a, b):
    return a // b


def band(a, b):
    return a & b


def bor(a, b):
    return a | b


def bxor(a, b):
    return a ^ b


def shl(a, b):
    if b >= 0:
        return a << b
    return a >> (-b)


def shr(a, b):
    if b >= 0:
        return a >> b
    return a << (-b)


def unm(a, b):
    return -a + (b-b)


def bnot(a, b):
    return ~a + (b-b)


class Arithmetic:
    operator = namedtuple('operator', ['integer_func', 'float_func'])
    operators = {
        ArithOp.ADD:  operator(add,  add),
        ArithOp.SUB:  operator(sub,  sub),
        ArithOp.MUL:  operator(mul,  mul),
        ArithOp.MOD:  operator(mod,  mod),
        ArithOp.POW:  operator(None, pow),
        ArithOp.DIV:  operator(None, div),
        ArithOp.IDIV: operator(fdiv, fdiv),
        ArithOp.BAND: operator(band, None),
        ArithOp.BOR:  operator(bor,  None),
        ArithOp.BXOR: operator(bxor, None),
        ArithOp.SHL:  operator(shl,  None),
        ArithOp.SHR:  operator(shr,  None),
        ArithOp.UNM:  operator(unm,  unm),
        ArithOp.BNOT: operator(bnot, None)
    }

    @staticmethod
    def arith(a, op, b):
        integer_func = Arithmetic.operators[op].integer_func
        float_func = Arithmetic.operators[op].float_func

        if float_func is None:
            x = LuaValue.to_integer(a)
            if x:
                y = LuaValue.to_integer(b)
                if y:
                    return integer_func(x, y)
        else:
            if integer_func is not None:
                if isinstance(a, int) and isinstance(b, int):
                    return integer_func(int(a), int(b))
            x = LuaValue.to_float(a)
            if x:
                y = LuaValue.to_float(b)
                if y:
                    return float_func(x, y)

        return None
