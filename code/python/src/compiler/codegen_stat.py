from compiler.lua_stat import *
from compiler.lua_exp import *
from compiler.codegen_exp import CodegenExp


class CodegenStat:
    @staticmethod
    def process(fi, stat):
        if isinstance(stat, FuncCallStat):
            CodegenStat.process_func_call_stat(fi, stat)
        elif isinstance(stat, BreakStat):
            CodegenStat.process_break_stat(fi)
        elif isinstance(stat, DoStat):
            CodegenStat.process_do_stat(fi, stat)
        elif isinstance(stat, WhileStat):
            CodegenStat.process_while_stat(fi, stat)
        elif isinstance(stat, RepeatStat):
            CodegenStat.process_repeat_stat(fi, stat)
        elif isinstance(stat, IfStat):
            CodegenStat.process_if_stat(fi, stat)
        elif isinstance(stat, ForNumStat):
            CodegenStat.process_for_num_stat(fi, stat)
        elif isinstance(stat, ForInStat):
            CodegenStat.process_for_in_stat(fi, stat)
        elif isinstance(stat, AssignStat):
            CodegenStat.process_assign_stat(fi, stat)
        elif isinstance(stat, LocalVarDeclStat):
            CodegenStat.process_local_var_decl_stat(fi, stat)
        elif isinstance(stat, LocalFuncDefStat):
            CodegenStat.process_local_func_def_stat(fi, stat)
        elif isinstance(stat, (LabelStat, GotoStat)):
            raise Exception('label and goto are not supported!')

    @staticmethod
    def process_local_func_def_stat(fi, stat):
        r = fi.add_local_var(stat.name, fi.pc() + 1)
        CodegenExp.process_func_def_exp(fi, stat.exp, r)

    @staticmethod
    def process_func_call_stat(fi, stat):
        r = fi.alloc_reg()
        CodegenExp.process_func_call_exp(fi, stat.exp, r, 0)
        fi.free_reg()

    @staticmethod
    def process_break_stat(fi):
        pc = fi.emit_jmp(0, 0)
        fi.add_break_jmp(pc)

    @staticmethod
    def process_do_stat(fi, stat):
        fi.enter_scope(False)
        from compiler.codegen_block import CodegenBlock
        CodegenBlock.gen_block(fi, stat.block)
        fi.close_open_upvals()
        fi.exit_scope(fi.pc() - 1)

    @staticmethod
    def process_while_stat(fi, stat):
        pc_before_exp = fi.pc()

        r = fi.alloc_reg()
        CodegenExp.process_exp(fi, stat.exp, r, 1)
        fi.free_reg()

        fi.emit_test(r, 0)
        pc_jmp_to_end = fi.emit_jmp(0, 0)

        fi.enter_scope(True)
        from compiler.codegen_block import CodegenBlock
        CodegenBlock.gen_block(fi, stat.block)
        fi.close_open_upvals()
        fi.emit_jmp(0, pc_before_exp - fi.pc() - 1)
        fi.exit_scope()

        fi.fix_sbx(pc_jmp_to_end, fi.pc() - pc_jmp_to_end)

    @staticmethod
    def process_repeat_stat(fi, stat):
        fi.enter_scope(True)

        pc_before_block = fi.pc()
        from compiler.codegen_block import CodegenBlock
        CodegenBlock.gen_block(fi, stat.block)

        r = fi.alloc_reg()
        CodegenExp.process_exp(fi, stat.exp, r, 1)
        fi.free_reg()

        fi.emit_test(r, 0)
        fi.emit_jmp(fi.get_jmp_arg_a(), pc_before_block-fi.pc()-1)
        fi.close_open_upvals()

        fi.exit_scope()

    @staticmethod
    def process_if_stat(fi, stat):
        pc_jmp_to_ends = []
        pc_jmp_to_next_exp = -1

        for i, exp in enumerate(stat.exps):
            if pc_jmp_to_next_exp >= 0:
                fi.fix_sbx(pc_jmp_to_next_exp, fi.pc() - pc_jmp_to_next_exp)

            r = fi.alloc_reg()
            CodegenExp.process_exp(fi, exp, r, 1)
            fi.free_reg()

            fi.emit_test(r, 0)
            pc_jmp_to_next_exp = fi.emit_jmp(0, 0)

            fi.enter_scope(False)
            from compiler.codegen_block import CodegenBlock
            CodegenBlock.gen_block(fi, stat.blocks[i])
            fi.close_open_upvals()
            fi.exit_scope()

            if i < len(stat.exps)-1:
                pc_jmp_to_ends.append(fi.emit_jmp(0, 0))
            else:
                pc_jmp_to_ends.append(pc_jmp_to_next_exp)

        for pc in pc_jmp_to_ends:
            fi.fix_sbx(pc, fi.pc() - pc)

    @staticmethod
    def process_for_num_stat(fi, stat):
        fi.enter_scope(True)
        local_var_stat = LocalVarDeclStat(0,
                                          ['(for index)', '(for limit)', '(for step)'],
                                          [stat.init_exp, stat.limit_exp, stat.step_exp])
        CodegenStat.process_local_var_decl_stat(fi, local_var_stat)
        fi.add_local_var(stat.var_name)

        a = fi.used_regs - 4
        pc_for_prep = fi.emit_for_prep(a, 0)
        from compiler.codegen_block import CodegenBlock
        CodegenBlock.gen_block(fi, stat.block)
        fi.close_open_upvals()
        pc_for_loop = fi.emit_for_loop(a, 0)

        fi.fix_sbx(pc_for_prep, pc_for_loop-pc_for_prep-1)
        fi.fix_sbx(pc_for_loop, pc_for_prep-pc_for_loop)

        fi.exit_scope()

    @staticmethod
    def process_for_in_stat(fi, stat):
        fi.enter_scope(True)

        local_var = LocalVarDeclStat(0,
                                     ['(for generator)', '(for state)', '(for control)'],
                                     stat.exp_list)
        CodegenStat.process_local_var_decl_stat(fi, local_var)
        for name in stat.name_list:
            fi.add_local_var(name)

        pc_jmp_to_tfc = fi.emit_jmp(0, 0)
        from compiler.codegen_block import CodegenBlock
        CodegenBlock.gen_block(fi, stat.block)
        fi.close_open_upvals()
        fi.fix_sbx(pc_jmp_to_tfc, fi.pc()-pc_jmp_to_tfc)

        r = fi.slot_of_local_var('(for generator)')
        fi.emit_tfor_call(r, len(stat.name_list))
        fi.emit_tfor_loop(r + 2, pc_jmp_to_tfc - fi.pc() - 1)

        fi.exit_scope()

    @staticmethod
    def process_local_var_decl_stat(fi, stat):
        exps = ExpHelper.remove_tail_nils(stat.exp_list)
        nexps = len(exps)
        nnames = len(stat.name_list)

        old_regs = fi.used_regs
        if nexps == nnames:
            for exp in exps:
                a = fi.alloc_reg()
                CodegenExp.process_exp(fi, exp, a, 1)
        elif nexps > nnames:
            for i in range(nexps):
                exp = exps[i]
                a = fi.alloc_reg()
                if i == nexps-1 and ExpHelper.is_vararg_or_func_call(exp):
                    CodegenExp.process_exp(fi, exp, a, 0)
                else:
                    CodegenExp.process_exp(fi, exp, a, 1)
        else:
            mult_ret = False
            for i in range(nexps):
                exp = exps[i]
                a = fi.alloc_reg()
                if i == nexps-1 and ExpHelper.is_vararg_or_func_call(exp):
                    mult_ret = True
                    n = nnames - nexps + 1
                    CodegenExp.process_exp(fi, exp, a, n)
                    fi.alloc_regs(n-1)
                else:
                    CodegenExp.process_exp(fi, exp, a, 1)

            if not mult_ret:
                n = nnames - nexps
                a = fi.alloc_regs(n)
                fi.emit_load_nil(a, n)

        fi.used_regs = old_regs
        for name in stat.name_list:
            fi.add_local_var(name)

    @staticmethod
    def process_assign_stat(fi, stat):
        exps = ExpHelper.remove_tail_nils(stat.exp_list)
        nexps = len(exps)
        nvars = len(stat.var_list)

        tregs = [0 for _ in range(nvars)]
        kregs = [0 for _ in range(nvars)]
        vregs = [0 for _ in range(nvars)]
        old_regs = fi.used_regs

        for i in range(len(stat.var_list)):
            exp = stat.var_list[i]
            if isinstance(exp, TableAccessExp):
                tregs[i] = fi.alloc_reg()
                CodegenExp.process_exp(fi, exp.prefix_exp, tregs[i], 1)
                kregs[i] = fi.alloc_reg()
                CodegenExp.process_exp(fi, exp.key_exp, kregs[i], 1)
            else:
                name = exp.name
                if fi.slot_of_local_var(name) < 0 and fi.index_of_upval(name) < 0:
                    kregs[i] = -1
                    if fi.index_of_constant(name) > 0xff:
                        kregs[i] = fi.alloc_reg()

        for i in range(nvars):
            vregs[i] = fi.used_regs + i

        if nexps >= nvars:
            for i in range(nexps):
                exp = exps[i]
                a = fi.alloc_reg()
                if i >= nvars and i == nexps-1 and ExpHelper.is_vararg_or_func_call(exp):
                    CodegenExp.process_exp(fi, exp, a, 0)
                else:
                    CodegenExp.process_exp(fi, exp, a, 1)
        else:
            mult_ret = False
            for i in range(nexps):
                exp = exps[i]
                a = fi.alloc_reg()
                if i == nexps-1 and ExpHelper.is_vararg_or_func_call(exp):
                    mult_ret = True
                    n = nvars - nexps + 1
                    CodegenExp.process_exp(fi, exp, a, n)
                    fi.alloc_regs(n-1)
                else:
                    CodegenExp.process_exp(fi, exp, a, 1)

            if not mult_ret:
                n = nvars - nexps
                a = fi.alloc_regs(n)
                fi.emit_load_nil(a, n)

        for i in range(nvars):
            exp = stat.var_list[i]
            if not isinstance(exp, NameExp):
                fi.emit_set_table(tregs[i], kregs[i], vregs[i])
                continue

            var_name = exp.name
            a = fi.slot_of_local_var(var_name)
            if a >= 0:
                fi.emit_move(a, vregs[i])
                continue

            b = fi.index_of_upval(var_name)
            if b >= 0:
                fi.emit_set_upval(vregs[i], b)
                continue

            a = fi.slot_of_local_var('_ENV')
            if a >= 0:
                if kregs[i] < 0:
                    b = 0x100 + fi.index_of_constant(var_name)
                    fi.emit_set_table(a, b, vregs[i])
                else:
                    fi.emit_set_table(a, kregs[i], vregs[i])
                continue

            a = fi.index_of_upval('_ENV')
            if kregs[i] < 0:
                b = 0x100 + fi.index_of_constant(var_name)
                fi.emit_set_tabup(a, b, vregs[i])
            else:
                fi.emit_set_tabup(a, kregs[i], vregs[i])

        fi.used_regs = old_regs
