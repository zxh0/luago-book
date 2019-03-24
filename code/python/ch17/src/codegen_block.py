from codegen_stat import CodegenStat
from codegen_exp import CodegenExp
from lua_exp import *


class CodegenBlock:
    @staticmethod
    def gen_block(funcinfo, block):
        for stat in block.stats:
            CodegenStat.process(funcinfo, stat)

        if block.ret_exps is not None:
            CodegenBlock.process_ret_stat(funcinfo, block.ret_exps)

    @staticmethod
    def process_ret_stat(fi, exps):
        nexps = len(exps)
        if nexps == 0:
            fi.emit_return(0, 0)
            return

        if nexps == 1:
            if isinstance(exps[0], NameExp):
                name_exp = exps[0]
                r = fi.slot_of_local_var(name_exp.name)
                if r >= 0:
                    fi.emit_return(r, 1)
                    return
            if isinstance(exps[0], FuncCallExp):
                func_exp = exps[0]
                r = fi.alloc_reg()
                CodegenExp.process_tail_call_exp(fi, func_exp, r)
                fi.free_reg()
                fi.emit_return(r, -1)
                return

        mult_ret = ExpHelper.is_vararg_or_func_call(exps[-1])
        for i in range(nexps):
            exp = exps[i]
            r = fi.alloc_reg()
            if i == nexps - 1 and mult_ret:
                CodegenExp.process_exp(fi, exp, r, -1)
            else:
                CodegenExp.process_exp(fi, exp, r, 1)
        fi.free_regs(nexps)

        a = fi.used_regs
        if mult_ret:
            fi.emit_return(a, -1)
        else:
            fi.emit_return(a, nexps)
