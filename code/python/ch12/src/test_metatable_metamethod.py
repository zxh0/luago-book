from lua_state import LuaState


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


def main():
    with open('./test/vector.luac', 'rb') as f:
        data = f.read()
        ls = LuaState()
        ls.register('print', py_print)
        ls.register('getmetatable', get_metatable)
        ls.register('setmetatable', set_metatable)
        ls.load(data)
        ls.call(0, 0)


if __name__ == '__main__':
    main()
