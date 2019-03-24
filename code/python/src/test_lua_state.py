from vm.lua_state import LuaState


def main():
    ls = LuaState()

    ls.push_boolean(True)
    ls.print_stack()
    ls.push_integer(10)
    ls.print_stack()
    ls.push_nil()
    ls.print_stack()
    ls.push_string('hello')
    ls.print_stack()
    ls.push_value(-4)
    ls.print_stack()
    ls.replace(3)
    ls.print_stack()
    ls.set_top(6)
    ls.print_stack()
    ls.remove(-3)
    ls.print_stack()
    ls.set_top(-5)
    ls.print_stack()


if __name__ == '__main__':
    main()
