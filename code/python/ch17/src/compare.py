from lua_table import LuaTable
from lua_value import LuaValue


class Compare:
    @staticmethod
    def eq(a, b, ls):
        if a is None:
            return b is None
        if isinstance(a, bool) or isinstance(a, str):
            return a == b
        if isinstance(a, int):
            if isinstance(b, int):
                return a == b
            elif isinstance(b, float):
                return float(a) == b
            else:
                return False
        if isinstance(a, float):
            if isinstance(b, float):
                return a == b
            elif isinstance(b, int):
                return a == float(b)
            else:
                return False
        if isinstance(a, LuaTable):
            if isinstance(b, LuaTable) and a != b and ls:
                mm = ls.get_metamethod(a, b, '__eq')
                if mm:
                    return LuaValue.to_boolean(ls.call_metamethod(a, mm, b))

        return a == b

    @staticmethod
    def lt(a, b, ls):
        if isinstance(a, str) and isinstance(b, str):
            return a < b
        if isinstance(a, int):
            if isinstance(b, int):
                return a < b
            elif isinstance(b, float):
                return float(a) < b
        if isinstance(a, float):
            if isinstance(b, float):
                return a < b
            elif isinstance(b, int):
                return a < float(b)

        mm = ls.get_metamethod(a, b, '__lt')
        if mm:
            return LuaValue.to_boolean(ls.call_metamethod(a, mm, b))

        raise Exception('Comparison Error')

    @staticmethod
    def le(a, b, ls):
        if isinstance(a, str) and isinstance(b, str):
            return a <= b
        if isinstance(a, int):
            if isinstance(b, int):
                return a <= b
            elif isinstance(b, float):
                return float(a) <= b
        if isinstance(a, float):
            if isinstance(b, float):
                return a <= b
            elif isinstance(b, int):
                return a <= float(b)

        mm = ls.get_metamethod(a, b, '__le')
        if mm:
            return LuaValue.to_boolean(ls.call_metamethod(a, mm, b))
        mm = ls.get_metamethod(b, a, '__lt')
        if mm:
            return LuaValue.to_boolean(ls.call_metamethod(a, mm, b))

        raise Exception('Comparison Error')
