from lua_state import LuaState
from arith_op import ArithOp
from cmp_op import CmpOp


def main():
    ls = LuaState(None)

    ls.push_integer(1)
    ls.push_string('2.0')
    ls.push_string('3.0')
    ls.push_number(4.0)
    ls.print_stack()

    ls.arith(ArithOp.ADD)
    ls.print_stack()
    ls.arith(ArithOp.BNOT)
    ls.print_stack()

    ls.len(2)
    ls.print_stack()
    ls.concat(3)
    ls.print_stack()
    ls.push_boolean(ls.compare(1, CmpOp.EQ, 2))
    ls.print_stack()

    ls.push_number(2)
    ls.push_number(2)
    ls.print_stack()
    ls.arith(ArithOp.POW)
    ls.print_stack()

    ls.push_number(3.0)
    ls.push_boolean(ls.compare(4, CmpOp.LT, 5))
    ls.print_stack()

    ls.push_string('hello')
    ls.push_string('world')
    ls.push_boolean(ls.compare(7, CmpOp.LE, 8))
    ls.print_stack()


if __name__ == '__main__':
    main()
