from enum import Enum, unique


@unique
class LuaType(Enum):
    NONE = -1
    NIL = 0
    BOOLEAN = 1
    LIGHT_USER_DATA = 2
    NUMBER = 3
    STRING = 4
    TABLE = 5
    FUNCTION = 6
    USER_DATA = 7
    THREAD = 8
