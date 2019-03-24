from vm.lua_state import LuaState


def main():
    ls = LuaState()
    ls.load('./lua/table.luac')
    ls.call(0, 0)


if __name__ == '__main__':
    main()
