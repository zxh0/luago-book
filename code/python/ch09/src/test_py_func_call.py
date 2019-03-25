from lua_state import LuaState
import sys


def py_print(ls):
    nargs = ls.get_top()
    for i in range(1, nargs+1):
        if ls.is_boolean(i):
            print('%t', ls.to_boolean(i), end='')
        elif ls.is_string(i):
            print(ls.to_string(i), end='')
        else:
            print(ls.type_name(ls.type(i)), end='')

        if i < nargs:
            print('\t', end='')

    print()
    return 0


def main():
    with open(sys.argv[1], 'rb') as f:
        data = f.read()
        ls = LuaState()
        ls.register('print', py_print)
        ls.load(data)
        ls.call(0, 0)


if __name__ == '__main__':
    main()
