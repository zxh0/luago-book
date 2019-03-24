from lua_state import LuaState


def main():
    with open('./test/function_call.luac', 'rb') as f:
        data = f.read()
        ls = LuaState()
        ls.load(data)
        ls.call(0, 0)


if __name__ == '__main__':
    main()
