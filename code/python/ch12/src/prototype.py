from upvalue import Upvalue
from local_var import LocalVar
from lua_opcode import Instruction


class Prototype:
    CONST_TYPE_NIL = 0x00
    CONST_TYPE_BOOLEAN = 0x01
    CONST_TYPE_NUMBER = 0x03
    CONST_TYPE_INTEGER = 0x13
    CONST_TYPE_SHORT_STR = 0x04
    CONST_TYPE_LONG_STR = 0x14

    def __init__(self, br, parent_source):
        self.source = br.read_lua_str()
        if self.source is None:
            self.source = parent_source
        self.line_defined = br.read_uint32()
        self.last_line_defined = br.read_uint32()
        self.num_params = br.read_uint8()
        self.is_vararg = br.read_uint8()
        self.max_stack_size = br.read_uint8()
        self.code = []
        self.read_code(br)
        self.constants = []
        self.read_constants(br)
        self.upvalues = []
        self.read_upvalues(br)
        self.protos = []
        self.read_protos(br, self.source)
        self.line_infos = []
        self.read_line_info(br)
        self.local_vars = []
        self.read_local_vars(br)
        self.upvalue_names = []
        self.read_upvalue_names(br)

    def read_code(self, br):
        self.code = []
        for i in range(br.read_uint32()):
            self.code.append(br.read_uint32())

    def read_constants(self, br):
        self.constants = []
        for i in range(br.read_uint32()):
            self.constants.append(Prototype.read_const(br))

    def read_upvalues(self, br):
        self.upvalues = []
        for i in range(br.read_uint32()):
            self.upvalues.append(Upvalue(br.read_uint8(), br.read_uint8()))

    def read_protos(self, br, parent_source):
        self.protos = []
        for i in range(br.read_uint32()):
            self.protos.append(Prototype(br, parent_source))

    def read_line_info(self, br):
        self.line_infos = []
        for i in range(br.read_uint32()):
            self.line_infos.append(br.read_uint32())

    def read_local_vars(self, br):
        self.local_vars = []
        for i in range(br.read_uint32()):
            var_name = br.read_lua_str()
            start_pc = br.read_uint32()
            end_pc = br.read_uint32()
            self.local_vars.append(LocalVar(var_name, start_pc, end_pc))

    def read_upvalue_names(self, br):
        self.upvalue_names = []
        for i in range(br.read_uint32()):
            self.upvalue_names.append(br.read_lua_str())

    def dump(self):
        self.print_header()
        self.print_code()
        self.print_detail()
        for p in self.protos:
            p.dump()

    def print_header(self):
        func_type = 'function' if self.last_line_defined else 'main'
        print('%s <%s:%d,%d> (%d instructions)' % (func_type, self.source, self.line_defined,
                                                   self.last_line_defined, len(self.code)))
        var_arg_flag = '+' if self.is_vararg > 0 else ''
        print('%d%s params, %d slots, %d upvalues, %d locals, %d constants, %d functions' %
              (self.num_params, var_arg_flag, self.max_stack_size, len(self.upvalues),
               len(self.local_vars), len(self.constants), len(self.protos)))

    def print_code(self):
        for i in range(len(self.code)):
            line = self.line_infos[i] if len(self.line_infos) > 0 else '-'
            inst = Instruction(self.code[i])
            print('\t%d\t[%s]\t' % (i+1, line), end='')
            inst.dump()

    def print_detail(self):
        # constants
        print('constants (%d):' % (len(self.constants)))
        for i in range(len(self.constants)):
            print('\t%d\t%s' % (i+1, Prototype.constant_to_string(self.constants[i])))

        # local vars
        print('locals (%d):' % len(self.local_vars))
        for i in range(len(self.local_vars)):
            lvar = self.local_vars[i]
            print('\t%d\t%s\t%d\t%d' % (i+1, lvar.var_name, lvar.start_pc+1, lvar.end_pc+1))

        # up values
        print('upvalues (%d):' % len(self.upvalues))
        for i in range(len(self.upvalues)):
            upval = self.upvalues[i]
            name = self.upvalue_names[i] if len(self.upvalue_names) > 0 else '-'
            print('\t%d\t%s\t%d\t%d' % (i+1, name, upval.instack, upval.idx))

    @staticmethod
    def constant_to_string(c):
        if c is None:
            return 'nil'
        elif isinstance(c, str):
            return '"' + c + '"'
        else:
            return str(c)

    @staticmethod
    def read_const(br):
        const_type = br.read_uint8()
        if const_type == Prototype.CONST_TYPE_NIL:
            return None
        elif const_type == Prototype.CONST_TYPE_BOOLEAN:
            return br.read_uint8() != 0
        elif const_type == Prototype.CONST_TYPE_INTEGER:
            return br.read_lua_int()
        elif const_type == Prototype.CONST_TYPE_NUMBER:
            return br.read_lua_number()
        elif const_type == Prototype.CONST_TYPE_SHORT_STR:
            return br.read_lua_str()
        elif const_type == Prototype.CONST_TYPE_LONG_STR:
            return br.read_lua_str()

    def get_code(self):
        return self.code

    def get_constants(self):
        return self.constants

    def get_max_stack_size(self):
        return self.max_stack_size

    def get_source(self):
        return self.source

    def get_line_defined(self):
        return self.line_defined

    def get_last_line_defined(self):
        return self.last_line_defined

    def get_num_params(self):
        return self.num_params

    def get_is_vararg(self):
        return self.is_vararg == 1

    def get_protos(self):
        return self.protos

    def get_upvalues(self):
        return self.upvalues
