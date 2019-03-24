from lua_exp import *
from func_info import FuncInfo
from lua_token import TokenKind
from lua_opcode import OpCode


class ArgAndKind:
    def __init__(self, arg, kind):
        self.arg = arg
        self.kind = kind


class CodegenExp:
    @staticmethod
    def process_exp(fi, exp, a, n):
        if isinstance(exp, NilExp):
            fi.emit_load_nil(a, n)
        elif isinstance(exp, FalseExp):
            fi.emit_load_bool(a, 0, 0)
        elif isinstance(exp, TrueExp):
            fi.emit_load_bool(a, 1, 0)
        elif isinstance(exp, IntegerExp):
            fi.emit_load_k(a, exp.val)
        elif isinstance(exp, FloatExp):
            fi.emit_load_k(a, exp.val)
        elif isinstance(exp, StringExp):
            fi.emit_load_k(a, exp.s)
        elif isinstance(exp, ParensExp):
            CodegenExp.process_exp(fi, exp.exp, a, 1)
        elif isinstance(exp, VarArgExp):
            CodegenExp.process_vararg_exp(fi, a, n)
        elif isinstance(exp, FuncDefExp):
            CodegenExp.process_func_def_exp(fi, exp, a)
        elif isinstance(exp, TableConstructorExp):
            CodegenExp.process_table_constructor_exp(fi, exp, a)
        elif isinstance(exp, UnopExp):
            CodegenExp.process_unop_exp(fi, exp, a)
        elif isinstance(exp, BinopExp):
            CodegenExp.process_binop_exp(fi, exp, a)
        elif isinstance(exp, ConcatExp):
            CodegenExp.process_concat_exp(fi, exp, a)
        elif isinstance(exp, NameExp):
            CodegenExp.process_name_exp(fi, exp, a)
        elif isinstance(exp, TableAccessExp):
            CodegenExp.process_table_access_exp(fi, exp, a)
        elif isinstance(exp, FuncCallExp):
            CodegenExp.process_func_call_exp(fi, exp, a, n)

    @staticmethod
    def process_vararg_exp(fi, a, n):
        if not fi.is_vararg:
            raise Exception('cannot use "..." outside a vararg function')
        fi.emit_vararg(a, n)

    @staticmethod
    def process_func_def_exp(fi, exp, a):
        from codegen_block import CodegenBlock
        sub_fi = FuncInfo(fi, exp)
        fi.sub_funcs.append(sub_fi)

        if exp.par_list is not None:
            for param in exp.par_list:
                sub_fi.add_local_var(param)

        CodegenBlock.gen_block(sub_fi, exp.block)
        sub_fi.exit_scope()
        sub_fi.emit_return(0, 0)

        bx = len(fi.sub_funcs) - 1
        fi.emit_closure(a, bx)

    @staticmethod
    def process_table_constructor_exp(fi, exp, a):
        narr = 0
        for key_exp in exp.key_exps:
            if key_exp is None:
                narr += 1

        nexps = len(exp.key_exps)
        mult_ret = nexps > 0 and ExpHelper.is_vararg_or_func_call(exp.val_exps[-1])
        fi.emit_new_table(a, narr, nexps-narr)

        arr_idx = 0
        for i in range(len(exp.key_exps)):
            key_exp = exp.key_exps[i]
            val_exp = exp.val_exps[i]

            if key_exp is None:
                arr_idx += 1
                tmp = fi.alloc_reg()
                if i == nexps - 1 and mult_ret:
                    CodegenExp.process_exp(fi, val_exp, tmp, -1)
                else:
                    CodegenExp.process_exp(fi, val_exp, tmp, 1)

                if arr_idx % 50 == 0 or arr_idx == narr:
                    n = arr_idx % 50
                    if n == 0:
                        n = 50

                    fi.free_regs(n)
                    c = (arr_idx - 1) // 50 + 1
                    if i == nexps - 1 and mult_ret:
                        fi.emit_set_list(a, 0, c)
                    else:
                        fi.emit_set_list(a, n, c)
                continue

            b = fi.alloc_reg()
            CodegenExp.process_exp(fi, key_exp, b, 1)
            c = fi.alloc_reg()
            CodegenExp.process_exp(fi, val_exp, c, 1)
            fi.free_regs(2)

            fi.emit_set_table(a, b, c)

    @staticmethod
    def process_unop_exp(fi, exp, a):
        b = fi.alloc_reg()
        CodegenExp.process_exp(fi, exp.exp, b, 1)
        fi.emit_unary_op(exp.op, a, b)
        fi.free_reg()

    @staticmethod
    def process_binop_exp(fi, exp, a):
        if exp.op == TokenKind.OP_AND or exp.op == TokenKind.OP_OR:
            b = fi.alloc_reg()
            CodegenExp.process_exp(fi, exp.exp1, b, 1)
            fi.free_reg()
            if exp.op == TokenKind.OP_AND:
                fi.emit_test_set(a, b, 0)
            else:
                fi.emit_test_set(a, b, 1)
            pc_of_jmp = fi.emit_jmp(0, 0)

            b = fi.alloc_reg()
            CodegenExp.process_exp(fi, exp.exp2, b, 1)
            fi.free_reg()
            fi.emit_move(a, b)
            fi.fix_sbx(pc_of_jmp, fi.pc()-pc_of_jmp)
        else:
            b = fi.alloc_reg()
            CodegenExp.process_exp(fi, exp.exp1, b, 1)
            c = fi.alloc_reg()
            CodegenExp.process_exp(fi, exp.exp2, c, 1)
            fi.emit_binary_op(exp.op, a, b, c)
            fi.free_regs(2)

    @staticmethod
    def process_concat_exp(fi, exp, a):
        for sub_exp in exp.exps:
            a1 = fi.alloc_reg()
            CodegenExp.process_exp(fi, sub_exp, a1, 1)

        c = fi.used_regs - 1
        b = c - len(exp.exps) + 1
        fi.free_regs(c - b + 1)
        fi.emit_abc(OpCode.CONCAT, a, b, c)

    @staticmethod
    def process_name_exp(fi, exp, a):
        r = fi.slot_of_local_var(exp.name)
        if r >= 0:
            fi.emit_move(a, r)
            return

        idx = fi.index_of_upval(exp.name)
        if idx >= 0:
            fi.emit_get_upval(a, idx)
            return

        prefix_exp = NameExp(exp.line, '_ENV')
        key_exp = StringExp(exp.line, exp.name)
        table_access_exp = TableAccessExp(exp.line, prefix_exp, key_exp)
        CodegenExp.process_table_access_exp(fi, table_access_exp, a)

    @staticmethod
    def process_table_access_exp(fi, exp, a):
        b = fi.alloc_reg()
        CodegenExp.process_exp(fi, exp.prefix_exp, b, 1)
        c = fi.alloc_reg()
        CodegenExp.process_exp(fi, exp.key_exp, c, 1)
        fi.emit_get_table(a, b, c)
        fi.free_regs(2)

    @staticmethod
    def process_func_call_exp(fi, exp, a, n):
        nargs = CodegenExp.process_prep_func_call(fi, exp, a)
        fi.emit_call(a, nargs, n)

    @staticmethod
    def process_tail_call_exp(fi, exp, a):
        nargs = CodegenExp.process_prep_func_call(fi, exp, a)
        fi.emit_tail_call(a, nargs)

    @staticmethod
    def process_prep_func_call(fi, exp, a):
        nargs = len(exp.args)
        last_arg_is_vararg_or_fkunc_call = False

        CodegenExp.process_exp(fi, exp.prefix_exp, a, 1)
        if exp.name_exp is not None:
            c = 0x100 + fi.index_of_constant(exp.name_exp.s)
            fi.emit_self(a, a, c)

        for i in range(len(exp.args)):
            arg = exp.args[i]
            tmp = fi.alloc_reg()
            if i == nargs - 1 and ExpHelper.is_vararg_or_func_call(arg):
                last_arg_is_vararg_or_fkunc_call = True
                CodegenExp.process_exp(fi, arg, tmp, -1)
            else:
                CodegenExp.process_exp(fi, arg, tmp, 1)
        fi.free_regs(nargs)

        if exp.name_exp is not None:
            nargs += 1
        if last_arg_is_vararg_or_fkunc_call:
            nargs = -1

        return nargs
