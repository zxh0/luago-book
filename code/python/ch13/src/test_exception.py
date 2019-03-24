from lua_state import LuaState
from thread_state import ThreadStatus


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


def error(ls):
    return ls.error()


def pcall(ls):
    nargs = ls.get_top() - 1
    status = ls.pcall(nargs, -1, 0)
    ls.push_boolean(status == ThreadStatus.OK)
    ls.insert(1)
    return ls.get_top()


def main():
    with open('./test/exception.luac', 'rb') as f:
        data = f.read()
        ls = LuaState()
        ls.register('print', py_print)
        ls.register('error', error)
        ls.register('pcall', pcall)
        ls.load(data)
        ls.call(0, 0)


if __name__ == '__main__':
    main()
