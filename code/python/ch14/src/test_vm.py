from binary_chunk import BinaryChunk
from lua_state import LuaState
from opcode import Instruction
from opcode import OpCode


def lua_main(proto):
    vm = LuaState(proto)
    print('max stack size: ', proto.get_max_stack_size())
    vm.set_top(proto.get_max_stack_size())
    while True:
        pc = vm.get_pc()
        i = vm.fetch()
        inst = Instruction(i)
        if inst.op_code() != OpCode.RETURN:
            inst.execute(vm)
            print('[%02d] %-8s ' % (pc+1, inst.op_name()), end='')
            vm.print_stack()
        else:
            break


def main():
    bc = BinaryChunk('./test/sum.luac')
    bc.print_header()
    bc.check_header()
    bc.print_main_func()

    proto = bc.get_main_func()
    lua_main(proto)


if __name__ == '__main__':
    main()
