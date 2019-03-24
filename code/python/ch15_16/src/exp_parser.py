from lua_token import TokenKind
import lua_exp
from optimizer import Optimizer
from lua_value import LuaValue


class ExpParser:
    @staticmethod
    def parse_exp_list(lexer):
        exps = [ExpParser.parse_exp(lexer)]
        while lexer.look_ahead() == TokenKind.SEP_COMMA:
            lexer.get_next_token()
            exps.append(ExpParser.parse_exp(lexer))
        return exps

    @staticmethod
    def parse_exp(lexer):
        return ExpParser.parse_exp12(lexer)

    # x or y
    @staticmethod
    def parse_exp12(lexer):
        exp = ExpParser.parse_exp11(lexer)
        while lexer.look_ahead() == TokenKind.OP_OR:
            line, op, _ = lexer.get_next_token()
            lor = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp11(lexer))
            exp = Optimizer.optimize_logical_or(lor)
        return exp

    # x and y
    @staticmethod
    def parse_exp11(lexer):
        exp = ExpParser.parse_exp10(lexer)
        while lexer.look_ahead() == TokenKind.OP_AND:
            line, op, _ = lexer.get_next_token()
            lor = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp10(lexer))
            exp = Optimizer.optimize_logical_and(lor)
        return exp

    # compare
    @staticmethod
    def parse_exp10(lexer):
        exp = ExpParser.parse_exp9(lexer)
        while True:
            kind = lexer.look_ahead()
            if kind in (TokenKind.OP_LT, TokenKind.OP_GT, TokenKind.OP_NE,
                        TokenKind.OP_LE, TokenKind.OP_GE, TokenKind.OP_EQ):
                line, op, _ = lexer.get_next_token()
                exp = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp9(lexer))
            else:
                return exp

    # x | y
    @staticmethod
    def parse_exp9(lexer):
        exp = ExpParser.parse_exp8(lexer)
        while lexer.look_ahead() == TokenKind.OP_BOR:
            line, op, _ = lexer.get_next_token()
            bor = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp8(lexer))
            exp = Optimizer.optimize_bitwise_binary_op(bor)
        return exp

    # x ~ y
    @staticmethod
    def parse_exp8(lexer):
        exp = ExpParser.parse_exp7(lexer)
        while lexer.look_ahead() == TokenKind.OP_BXOR:
            line, op, _ = lexer.get_next_token()
            bor = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp8(lexer))
            exp = Optimizer.optimize_bitwise_binary_op(bor)
        return exp

    # x & y
    @staticmethod
    def parse_exp7(lexer):
        exp = ExpParser.parse_exp6(lexer)
        while lexer.look_ahead() == TokenKind.OP_BAND:
            line, op, _ = lexer.get_next_token()
            bor = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp8(lexer))
            exp = Optimizer.optimize_bitwise_binary_op(bor)
        return exp

    # shift
    @staticmethod
    def parse_exp6(lexer):
        exp = ExpParser.parse_exp5(lexer)
        if lexer.look_ahead() in (TokenKind.OP_SHL, TokenKind.OP_SHR):
            line, op, _ = lexer.get_next_token()
            shx = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp5(lexer))
            exp = Optimizer.optimize_bitwise_binary_op(shx)
        else:
            return exp
        return exp

    # a .. b
    @staticmethod
    def parse_exp5(lexer):
        exp = ExpParser.parse_exp4(lexer)
        if lexer.look_ahead() != TokenKind.OP_CONCAT:
            return exp

        line = 0
        exps = []
        while lexer.look_ahead() == TokenKind.OP_CONCAT:
            line, _, _ = lexer.get_next_token()
            exps.append(ExpParser.parse_exp4(lexer))
        return lua_exp.ConcatExp(line, exps)

    # x +/- y
    @staticmethod
    def parse_exp4(lexer):
        exp = ExpParser.parse_exp3(lexer)
        while True:
            if lexer.look_ahead() in (TokenKind.OP_ADD, TokenKind.OP_SUB):
                line, op, _ = lexer.get_next_token()
                arith = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp3(lexer))
                exp = Optimizer.optimize_arith_binary_op(arith)
            else:
                break
        return exp

    # *, %, /, //
    @staticmethod
    def parse_exp3(lexer):
        exp = ExpParser.parse_exp2(lexer)
        while True:
            if lexer.look_ahead() in (TokenKind.OP_MUL, TokenKind.OP_MOD, TokenKind.OP_DIV, TokenKind.OP_IDIV):
                line, op, _ = lexer.get_next_token()
                arith = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp2(lexer))
                exp = Optimizer.optimize_arith_binary_op(arith)
            else:
                break
        return exp

    # unary
    @staticmethod
    def parse_exp2(lexer):
        if lexer.look_ahead() in (TokenKind.OP_UNM, TokenKind.OP_BNOT, TokenKind.OP_LEN, TokenKind.OP_NOT):
            line, op, _ = lexer.get_next_token()
            exp = lua_exp.UnopExp(line, op, ExpParser.parse_exp2(lexer))
            return Optimizer.optimize_unary_op(exp)
        return ExpParser.parse_exp1(lexer)

    # x ^ y
    @staticmethod
    def parse_exp1(lexer):
        exp = ExpParser.parse_exp0(lexer)
        if lexer.look_ahead() == TokenKind.OP_POW:
            line, op, _ = lexer.get_next_token()
            exp = lua_exp.BinopExp(line, op, exp, ExpParser.parse_exp2(lexer))
        return Optimizer.optimize_pow(exp)

    @staticmethod
    def parse_exp0(lexer):
        kind = lexer.look_ahead()
        if kind == TokenKind.VARARG:
            line, _, _ = lexer.get_next_token()
            return lua_exp.VarArgExp(line)
        if kind == TokenKind.KW_NIL:
            line, _, _ = lexer.get_next_token()
            return lua_exp.NilExp(line)
        if kind == TokenKind.KW_TRUE:
            line, _, _ = lexer.get_next_token()
            return lua_exp.TrueExp(line)
        if kind == TokenKind.KW_FALSE:
            line, _, _ = lexer.get_next_token()
            return lua_exp.FalseExp(line)
        if kind == TokenKind.STRING:
            line, _, token = lexer.get_next_token()
            return lua_exp.StringExp(line, token)
        if kind == TokenKind.NUMBER:
            return ExpParser.parse_number_exp(lexer)
        if kind == TokenKind.SEP_LCURLY:
            return ExpParser.parse_table_constructor_exp(lexer)
        if kind == TokenKind.KW_FUNCTION:
            lexer.get_next_token()
            return ExpParser.parse_func_def_exp(lexer)
        return ExpParser.parse_prefix_exp(lexer)

    @staticmethod
    def parse_number_exp(lexer):
        line, _, token = lexer.get_next_token()
        i = LuaValue.parse_integer(token)
        if i is not None:
            return lua_exp.IntegerExp(line, i)
        f = LuaValue.parse_float(token)
        if f is not None:
            return lua_exp.FloatExp(line, f)
        raise Exception('not a number: ' + token)

    # functiondef::= function funcbody
    # funcbody::= '(' [parlist] ')' block end
    @staticmethod
    def parse_func_def_exp(lexer):
        from parser import Parser
        line = lexer.get_line()
        lexer.get_next_token_of_kind(TokenKind.SEP_LPAREN)
        par_list, is_var_arg = ExpParser.parse_par_list(lexer)
        lexer.get_next_token_of_kind(TokenKind.SEP_RPAREN)
        block = Parser.parse_block(lexer)
        last_line, _ = lexer.get_next_token_of_kind(TokenKind.KW_END)
        return lua_exp.FuncDefExp(line, last_line, par_list, is_var_arg, block)

    # [parlist]
    # parlist ::= namelist [',' '...'] | '...'
    @staticmethod
    def parse_par_list(lexer):
        kind = lexer.look_ahead()
        if kind == TokenKind.SEP_RPAREN:
            return None, False
        if kind == TokenKind.VARARG:
            return None, True

        _, name = lexer.get_next_identifier()
        names = [name]
        is_var_arg = False
        while lexer.look_ahead() == TokenKind.SEP_COMMA:
            lexer.get_next_token()
            if lexer.look_ahead() == TokenKind.IDENTIFIER:
                _, name = lexer.get_next_identifier()
                names.append(name)
            else:
                lexer.get_next_token_of_kind(TokenKind.VARARG)
                is_var_arg = True
                break
        return names, is_var_arg

    # tableconstructor ::= '{' [fieldlist] '}'
    @staticmethod
    def parse_table_constructor_exp(lexer):
        line = lexer.get_line()
        lexer.get_next_token_of_kind(TokenKind.SEP_LCURLY)
        key_exps, val_exps = ExpParser.parse_field_list(lexer)
        lexer.get_next_token_of_kind(TokenKind.SEP_RCURLY)
        last_line = lexer.get_line()
        return lua_exp.TableConstructorExp(line, last_line, key_exps, val_exps)

    # fieldlist ::= field {fieldsep field} [fieldsep]
    @staticmethod
    def parse_field_list(lexer):
        ks = []
        vs = []
        if lexer.look_ahead != TokenKind.SEP_RCURLY:
            k, v = ExpParser.parse_field(lexer)
            ks.append(k)
            vs.append(v)

            while ExpParser.is_field_sep(lexer.look_ahead()):
                lexer.get_next_token()
                if lexer.look_ahead() != TokenKind.SEP_RCURLY:
                    k, v = ExpParser.parse_field(lexer)
                    ks.append(k)
                    vs.append(v)
                else:
                    break
        return ks, vs

    # fieldsep ::= ',' | ';'
    @staticmethod
    def is_field_sep(kind):
        return kind in (TokenKind.SEP_COMMA, TokenKind.SEP_SEMI)

    # field ::= '[' exp ']' '=' exp | Name '=' exp | exp
    @staticmethod
    def parse_field(lexer):
        if lexer.look_ahead() == TokenKind.SEP_LBRACK:
            lexer.get_next_token()
            k = ExpParser.parse_exp(lexer)
            lexer.get_next_token_of_kind(TokenKind.SEP_RBRACK)
            lexer.get_next_token_of_kind(TokenKind.OP_ASSIGN)
            v = ExpParser.parse_exp(lexer)
            return k, v

        exp = ExpParser.parse_exp(lexer)
        if isinstance(exp, lua_exp.NameExp):
            if lexer.look_ahead() == TokenKind.OP_ASSIGN:
                lexer.get_next_token()
                k = lua_exp.StringExp(exp.line, exp.name)
                v = ExpParser.parse_exp(lexer)
                return k, v

        return None, exp

    # prefixexp::= var | functioncall | '(' exp ')"
    # var::= Name | prefixexp '[' exp ']' | prefixexp '.' Name
    # functioncall ::=  prefixexp
    # args | prefixexp ':' Name
    # args

    # prefixexp::= Name
    #   | '(' exp ')'
    #   | prefixexp '[' exp ']'
    #   | prefixexp '.' Name
    #   | prefixexp[':' Name] args
    @staticmethod
    def parse_prefix_exp(lexer):
        if lexer.look_ahead() == TokenKind.IDENTIFIER:
            line, name = lexer.get_next_identifier()
            exp = lua_exp.NameExp(line, name)
        else:
            exp = ExpParser.parse_parens_exp(lexer)
        return ExpParser.finish_prefix_exp(lexer, exp)

    @staticmethod
    def parse_parens_exp(lexer):
        lexer.get_next_token_of_kind(TokenKind.SEP_LPAREN)
        exp = ExpParser.parse_exp(lexer)
        lexer.get_next_token_of_kind(TokenKind.SEP_RPAREN)

        if isinstance(exp, lua_exp.VarArgExp) or isinstance(exp, lua_exp.FuncCallExp) or \
                isinstance(exp, lua_exp.NameExp) or isinstance(exp, lua_exp.TableAccessExp):
            return lua_exp.ParensExp(exp)
        return exp

    @staticmethod
    def finish_prefix_exp(lexer, exp):
        while True:
            kind = lexer.look_ahead()
            if kind == TokenKind.SEP_LBRACK:
                lexer.get_next_token()
                key_exp = ExpParser.parse_exp(lexer)
                lexer.get_next_token_of_kind(TokenKind.SEP_RBRACK)
                exp = lua_exp.TableAccessExp(lexer.get_line(), exp, key_exp)
            elif kind == TokenKind.SEP_DOT:
                lexer.get_next_token()
                line, name = lexer.get_next_identifier()
                key_exp = lua_exp.StringExp(line, name)
                exp = lua_exp.TableAccessExp(line, exp, key_exp)
            elif kind in (TokenKind.SEP_COLON, TokenKind.SEP_LPAREN, TokenKind.SEP_LCURLY, TokenKind.STRING):
                exp = ExpParser.finish_func_call_exp(lexer, exp)
            return exp

    # functioncall ::= prefixexp args | prefixexp ':' Name args
    @staticmethod
    def finish_func_call_exp(lexer, prefix_exp):
        name_exp = ExpParser.parse_name_exp(lexer)
        line = lexer.get_line()
        args = ExpParser.parse_args(lexer)
        last_line = lexer.get_line()
        return lua_exp.FuncCallExp(line, last_line, prefix_exp, name_exp, args)

    @staticmethod
    def parse_name_exp(lexer):
        if lexer.look_ahead() == TokenKind.SEP_COLON:
            lexer.get_next_token()
            line, name = lexer.get_next_identifier()
            return lua_exp.StringExp(line, name)
        return None

    # args ::= '(' [explist] ')' | tableconstructor | LitreralString
    @staticmethod
    def parse_args(lexer):
        args = []
        kind = lexer.look_ahead()
        if kind == TokenKind.SEP_LPAREN:    # '(' [explist] ')'
            lexer.get_next_token()
            if lexer.look_ahead() != TokenKind.SEP_RPAREN:
                args = ExpParser.parse_exp_list(lexer)
            lexer.get_next_token_of_kind(TokenKind.SEP_RPAREN)
        elif kind == TokenKind.SEP_LCURLY:  # '{' [fieldlist] '}'
            args = [ExpParser.parse_table_constructor_exp(lexer)]
        else:                               # LiteralString
            line, s = lexer.get_next_token_of_kind(TokenKind.STRING)
            args = [lua_exp.StringExp(line, s)]
        return args
