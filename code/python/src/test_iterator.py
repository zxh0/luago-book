from vm.lua_state import LuaState
from vm.lua_type import LuaType


def py_print(ls):
    nargs = ls.get_top()
    for i in range(1, nargs+1):
        if ls.is_boolean(i):
            print('true' if ls.to_boolean(i) else 'false', end='')
        elif ls.is_string(i):
            print(ls.to_string(i), end='')
        else:
            print(ls.type_name(ls.type(i)), end='')

        if i < nargs:
            print('\t', end='')

    print()
    return 0


def get_metatable(ls):
    if not ls.get_metatable:
        ls.push_nil()
    return 1


def set_metatable(ls):
    ls.set_metatable(1)
    return 1


def lua_next(ls):
    ls.set_top(2)
    if ls.next(1):
        return 2
    else:
        ls.push_nil()
        return 1


def pairs(ls):
    ls.push_py_function(lua_next)
    ls.push_value(1)
    ls.push_nil()
    return 3


def ipairs(ls):
    ls.push_py_function(ipairs_aux)
    ls.push_value(1)
    ls.push_integer(0)
    return 3


def ipairs_aux(ls):
    i = ls.to_integer(2) + 1
    ls.push_integer(i)
    if ls.get_i(1, i) == LuaType.NIL:
        return 1
    else:
        return 2


def main():
    ls = LuaState()
    ls.register('print', py_print)
    ls.register('getmetatable', get_metatable)
    ls.register('setmetatable', set_metatable)
    ls.register('next', lua_next)
    ls.register('pairs', pairs)
    ls.register('ipairs', ipairs)
    ls.load('./lua/iterator.luac')
    ls.call(0, 0)


if __name__ == '__main__':
    main()
