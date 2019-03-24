from lua_exp import FuncDefExp
from func_info import FuncInfo
from codegen_exp import CodegenExp


class Codegen:
    @staticmethod
    def gen_proto(chunk):
        func_def_exp = FuncDefExp(0, chunk.last_line, [], True, chunk)
        func_info = FuncInfo(None, func_def_exp)
        func_info.add_local_var('_ENV')

        CodegenExp.process_func_def_exp(func_info, func_def_exp, 0)
        return func_info.sub_funcs[0].to_proto()

