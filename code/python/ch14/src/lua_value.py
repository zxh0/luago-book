from lua_type import LuaType


class LuaValue:
    @staticmethod
    def type_of(val):
        from lua_table import LuaTable
        from closure import Closure
        if val is None:
            return LuaType.NIL
        elif isinstance(val, bool):
            return LuaType.BOOLEAN
        elif isinstance(val, int) or isinstance(val, float):
            return LuaType.NUMBER
        elif isinstance(val, str):
            return LuaType.STRING
        elif isinstance(val, LuaTable):
            return LuaType.TABLE
        elif isinstance(val, Closure):
            return LuaType.FUNCTION
        return LuaType.NONE

    @staticmethod
    def to_boolean(val):
        if val is None:
            return False
        elif isinstance(val, bool):
            return val
        else:
            return True

    @staticmethod
    def is_integer(val):
        return val == int(val)

    @staticmethod
    def parse_integer(s):
        try:
            return int(s)
        except ValueError:
            return None

    @staticmethod
    def parse_float(s):
        try:
            return float(s)
        except ValueError:
            return None

    @staticmethod
    def to_integer(val):
        if isinstance(val, int):
            return val
        elif isinstance(val, float):
            return int(val) if LuaValue.is_integer(val) else None
        elif isinstance(val, str):
            return LuaValue.parse_integer(val)

    @staticmethod
    def to_float(val):
        if isinstance(val, float):
            return val
        elif isinstance(val, int):
            return float(val)
        elif isinstance(val, str):
            return LuaValue.parse_float(val)

    @staticmethod
    def float2integer(val):
        if isinstance(val, float):
            if LuaValue.is_integer(val):
                return int(val)
        return val

    @staticmethod
    def fb2int(val):
        if val < 8:
            return val
        return ((val & 7) + 8) << ((val >> 3) - 1)
