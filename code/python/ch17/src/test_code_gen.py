from lua_state import LuaState
from lua_type import LuaType
from thread_state import ThreadStatus
import sys


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


def error(ls):
    return ls.error()


def pcall(ls):
    nargs = ls.get_top() - 1
    status = ls.pcall(nargs, -1, 0)
    ls.push_boolean(status == ThreadStatus.OK)
    ls.insert(1)
    return ls.get_top()


def main():
    ls = LuaState()
    ls.register('print', py_print)
    ls.register('getmetatable', get_metatable)
    ls.register('setmetatable', set_metatable)
    ls.register('next', lua_next)
    ls.register('pairs', pairs)
    ls.register('ipairs', ipairs)
    ls.register('error', error)
    ls.register('pcall', pcall)

    ls.load(sys.argv[1])
    ls.call(0, 0)


if __name__ == '__main__':
    if len(sys.argv) == 2:
        main()
    else:
        print('Error argument')
