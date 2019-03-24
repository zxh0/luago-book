from collections import namedtuple
from vm.lua_value import LuaValue
from vm.arith_op import ArithOp


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
    operator = namedtuple('operator', ['metamethod', 'integer_func', 'float_func'])
    operators = {
        ArithOp.ADD:  operator('__add',  add,  add),
        ArithOp.SUB:  operator('__sub',  sub,  sub),
        ArithOp.MUL:  operator('__mul',  mul,  mul),
        ArithOp.MOD:  operator('__mod',  mod,  mod),
        ArithOp.POW:  operator('__pow',  None, pow),
        ArithOp.DIV:  operator('__div',  None, div),
        ArithOp.IDIV: operator('__fdiv', fdiv, fdiv),
        ArithOp.BAND: operator('__band', band, None),
        ArithOp.BOR:  operator('__bor',  bor,  None),
        ArithOp.BXOR: operator('__bxor', bxor, None),
        ArithOp.SHL:  operator('__shl',  shl,  None),
        ArithOp.SHR:  operator('__shr',  shr,  None),
        ArithOp.UNM:  operator('__unm',  unm,  unm),
        ArithOp.BNOT: operator('__bnot', bnot, None)
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
