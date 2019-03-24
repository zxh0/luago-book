from compiler.lua_token import TokenKind
from compiler import lua_exp
from vm.lua_value import LuaValue


class Optimizer:
    # or
    @staticmethod
    def optimize_logical_or(exp):
        if Optimizer.is_true(exp.exp1):
            # true or x => true
            return exp.exp1
        if Optimizer.is_false(exp.exp1) and not Optimizer.is_var_arg_or_func_call(exp.exp2):
            # false or x => x
            return exp.exp2
        return exp

    # and
    @staticmethod
    def optimize_logical_and(exp):
        if Optimizer.is_false(exp.exp1):
            # false and x => false
            return exp.exp1
        if Optimizer.is_true(exp.exp1) and not Optimizer.is_var_arg_or_func_call(exp.exp2):
            # true and x => x
            return exp.exp2
        return exp

    # & | ~ << >>
    @staticmethod
    def optimize_bitwise_binary_op(exp):
        i, oki = Optimizer.cast_to_int(exp.exp1)
        j, okj = Optimizer.cast_to_int(exp.exp2)
        if oki and okj:
            if exp.op == TokenKind.OP_BAND:
                return lua_exp.IntegerExp(exp.line, i & j)
            if exp.op == TokenKind.OP_BOR:
                return lua_exp.IntegerExp(exp.line, i | j)
            if exp.op == TokenKind.OP_BXOR:
                return lua_exp.IntegerExp(exp.line, i ^ j)
            if exp.op == TokenKind.OP_SHL:
                return lua_exp.IntegerExp(exp.line, i << j)
            if exp.op == TokenKind.OP_SHR:
                return lua_exp.IntegerExp(exp.line, i >> j)
        return exp

    # + - * / // %
    @staticmethod
    def optimize_arith_binary_op(exp):
        if isinstance(exp.exp1, lua_exp.IntegerExp):
            if isinstance(exp.exp2, lua_exp.IntegerExp):
                if exp.op == TokenKind.OP_ADD:
                    return lua_exp.IntegerExp(exp.line, exp.exp1.val + exp.exp2.val)
                if exp.op == TokenKind.OP_SUB:
                    return lua_exp.IntegerExp(exp.line, exp.exp1.val - exp.exp2.val)
                if exp.op == TokenKind.OP_MUL:
                    return lua_exp.IntegerExp(exp.line, exp.exp1.val * exp.exp2.val)
                if exp.op == TokenKind.OP_IDIV:
                    if exp.exp2.val != 0:
                        return lua_exp.IntegerExp(exp.line, exp.exp1.val // exp.exp2.val)
                if exp.op == TokenKind.OP_MOD:
                    if exp.exp2.val != 0:
                        return lua_exp.IntegerExp(exp.line, exp.exp1.val % exp.exp2.val)

        f, okf = Optimizer.cast_to_float(exp.exp1)
        g, okg = Optimizer.cast_to_float(exp.exp2)
        if okf and okg:
            if exp.op == TokenKind.OP_ADD:
                return lua_exp.IntegerExp(exp.line, f + g)
            if exp.op == TokenKind.OP_SUB:
                return lua_exp.IntegerExp(exp.line, f - g)
            if exp.op == TokenKind.OP_MUL:
                return lua_exp.IntegerExp(exp.line, f * g)
            if exp.op == TokenKind.OP_DIV:
                if g != 0:
                    return lua_exp.IntegerExp(exp.line, f / g)
            if exp.op == TokenKind.OP_IDIV:
                if g != 0:
                    return lua_exp.IntegerExp(exp.line, f // g)
            if exp.op == TokenKind.OP_MOD:
                if g != 0:
                    return lua_exp.IntegerExp(exp.line, f % g)
            if exp.op == TokenKind.OP_POW:
                return lua_exp.IntegerExp(exp.line, f ** g)
        return exp

    # ^
    @staticmethod
    def optimize_pow(exp):
        if isinstance(exp, lua_exp.BinopExp):
            if exp.op == TokenKind.OP_POW:
                exp.exp2 = Optimizer.optimize_pow(exp.exp2)
            return Optimizer.optimize_arith_binary_op(exp)
        return exp

    # - not ~
    @staticmethod
    def optimize_unary_op(exp):
        if exp.op == TokenKind.OP_UNM:
            return Optimizer.optimize_unm(exp)
        if exp.op == TokenKind.OP_NOT:
            return Optimizer.optimize_not(exp)
        if exp.op == TokenKind.OP_BNOT:
            return Optimizer.optimize_bnot(exp)
        return exp

    @staticmethod
    def optimize_unm(exp):
        if isinstance(exp.exp, lua_exp.IntegerExp):
            exp.exp.val = -exp.exp.val
            return exp.exp
        if isinstance(exp.exp, lua_exp.FloatExp):
            if exp.exp.val != 0:
                exp.exp.val = -exp.exp.val
                return exp.val
        return exp

    # not
    @staticmethod
    def optimize_not(exp):
        if isinstance(exp.exp, lua_exp.NilExp) or isinstance(exp.exp, lua_exp.FalseExp):
            return lua_exp.TrueExp(exp.line)
        if isinstance(exp.exp, lua_exp.TrueExp) or isinstance(exp.exp, lua_exp.FloatExp) or \
           isinstance(exp.exp, lua_exp.StringExp):
            return lua_exp.FalseExp(exp.line)
        return exp

    # ~
    @staticmethod
    def optimize_bnot(exp):
        if isinstance(exp.exp, lua_exp.IntegerExp):
            exp.exp.val = ~exp.exp.val
            return exp.exp.val
        if isinstance(exp.exp, lua_exp.FloatExp):
            i = LuaValue.float2integer(exp.exp.val)
            if i is not None:
                return lua_exp.IntegerExp(exp.exp.line, ~i)
        return exp

    # false
    @staticmethod
    def is_false(exp):
        if isinstance(exp, lua_exp.FalseExp) or isinstance(exp, lua_exp.NilExp):
            return True
        return False

    # true
    @staticmethod
    def is_true(exp):
        if isinstance(exp, lua_exp.TrueExp) or isinstance(exp, lua_exp.IntegerExp) or \
           isinstance(exp, lua_exp.FloatExp) or isinstance(exp, lua_exp.StringExp):
            return True
        return False

    @staticmethod
    def is_var_arg_or_func_call(exp):
        if isinstance(exp, lua_exp.VarArgExp) or isinstance(exp, lua_exp.FuncCallExp):
            return True
        return False

    @staticmethod
    def cast_to_int(exp):
        if isinstance(exp, lua_exp.IntegerExp):
            return exp.val, True
        if isinstance(exp, lua_exp.FloatExp):
            i = LuaValue.float2integer(exp.val)
            return i, i is not None
        return 0, False

    @staticmethod
    def cast_to_float(exp):
        if isinstance(exp, lua_exp.IntegerExp):
            return float(exp.val), True
        if isinstance(exp, lua_exp.FloatExp):
            return exp.val, True
        return 0, False
