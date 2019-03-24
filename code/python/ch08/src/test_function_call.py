from lua_state import LuaState
import sys


def main():
    with open(sys.argv[1], 'rb') as f:
        data = f.read()
        ls = LuaState()
        ls.load(data)
        ls.call(0, 0)


if __name__ == '__main__':
    main()
