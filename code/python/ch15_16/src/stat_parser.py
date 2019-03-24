from lua_token import TokenKind
import lua_stat
import lua_exp
from exp_parser import ExpParser
from parser import Parser


"""
stat ::=  ‘;’
    | break
    | ‘::’ Name ‘::’
    | goto Name
    | do block end
    | while exp do block end
    | repeat block until exp
    | if exp then block {elseif exp then block} [else block] end
    | for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
    | for namelist in explist do block end
    | function funcname funcbody
    | local function Name funcbody
    | local namelist [‘=’ explist]
    | varlist ‘=’ explist
    | functioncall
"""


class StatParser:
    @staticmethod
    def parse_stat(lexer):
        kind = lexer.look_ahead()
        if kind == TokenKind.SEP_SEMI:
            return StatParser.parse_empty_stat(lexer)
        if kind == TokenKind.KW_BREAK:
            return StatParser.parse_break_stat(lexer)
        if kind == TokenKind.SEP_LABEL:
            return StatParser.parse_label_stat(lexer)
        if kind == TokenKind.KW_GOTO:
            return StatParser.parse_goto_stat(lexer)
        if kind == TokenKind.KW_DO:
            return StatParser.parse_do_stat(lexer)
        if kind == TokenKind.KW_WHILE:
            return StatParser.parse_while_stat(lexer)
        if kind == TokenKind.KW_REPEAT:
            return StatParser.parse_repeat_stat(lexer)
        if kind == TokenKind.KW_IF:
            return StatParser.parse_if_stat(lexer)
        if kind == TokenKind.KW_FOR:
            return StatParser.parse_for_stat(lexer)
        if kind == TokenKind.KW_FUNCTION:
            return StatParser.parse_func_def_stat(lexer)
        if kind == TokenKind.KW_LOCAL:
            return StatParser.parse_local_assign_or_func_def_stat(lexer)
        return StatParser.parse_assign_or_func_call_stat(lexer)

    # ;
    @staticmethod
    def parse_empty_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.SEP_SEMI)
        return lua_stat.EmptyStat(lexer.get_line())

    # break
    @staticmethod
    def parse_break_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_BREAK)
        return lua_stat.BreakStat(lexer.get_line())

    # '::' Name '::'
    @staticmethod
    def parse_label_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.SEP_LABEL)
        _, name = lexer.get_next_identifier()
        lexer.get_next_token_of_kind(TokenKind.SEP_LABEL)
        return lua_stat.LabelStat(name)

    # goto Name
    @staticmethod
    def parse_goto_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_GOTO)
        _, name = lexer.get_next_identifier()
        return lua_stat.GotoStat(name)

    # do block end
    @staticmethod
    def parse_do_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_DO)
        block = Parser.parse_block(lexer)
        lexer.get_next_token_of_kind(TokenKind.KW_END)
        return lua_stat.DoStat(block)

    # while exp do block end
    @staticmethod
    def parse_while_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_WHILE)
        exp = ExpParser.parse_exp(lexer)
        lexer.get_next_token_of_kind(TokenKind.KW_DO)
        block = Parser.parse_block(lexer)
        lexer.get_next_token_of_kind(TokenKind.KW_END)
        return lua_stat.WhileStat(exp, block)

    # repeat block until exp
    @staticmethod
    def parse_repeat_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_REPEAT)
        block = Parser.parse_block(lexer)
        lexer.get_next_token_of_kind(TokenKind.KW_UNTIL)
        exp = ExpParser.parse_exp(lexer)
        return lua_stat.RepeatStat(block, exp)

    # if exp then block {elseif exp then block} [else block] end
    @staticmethod
    def parse_if_stat(lexer):
        exps = []
        blocks = []

        lexer.get_next_token_of_kind(TokenKind.KW_IF)
        exps.append(ExpParser.parse_exp(lexer))
        lexer.get_next_token_of_kind(TokenKind.KW_THEN)
        blocks.append(Parser.parse_block(lexer))

        while lexer.look_ahead() == TokenKind.KW_ELSEIF:
            lexer.get_next_token()
            exps.append(ExpParser.parse_exp(lexer))
            lexer.get_next_token_of_kind(TokenKind.KW_THEN)
            blocks.append(Parser.parse_block(lexer))

        if lexer.look_ahead() == TokenKind.KW_ELSE:
            lexer.get_next_token()
            exps.append(lua_exp.TrueExp(lexer.get_line()))
            blocks.append(Parser.parse_block(lexer))

        lexer.get_next_token_of_kind(TokenKind.KW_END)
        return lua_stat.IfStat(exps, blocks)

    # for Name '=' exp ',' exp [',' exp] do block end
    # for namelist in explist do block end
    @staticmethod
    def parse_for_stat(lexer):
        line_of_for, _ = lexer.get_next_token_of_kind(TokenKind.KW_FOR)
        _, name = lexer.get_next_identifier()
        if lexer.look_ahead() == TokenKind.OP_ASSIGN:
            return StatParser.finish_for_num_stat(lexer, line_of_for, name)
        else:
            return StatParser.finish_for_in_stat(lexer, name)

    # for Name '=' exp ',' exp [',' exp] do block end
    @staticmethod
    def finish_for_num_stat(lexer, line_of_for, var_name):
        lexer.get_next_token_of_kind(TokenKind.OP_ASSIGN)
        init_exp = ExpParser.parse_exp(lexer)
        lexer.get_next_token_of_kind(TokenKind.SEP_COMMA)
        limit_exp = ExpParser.parse_exp(lexer)

        if lexer.look_ahead() == TokenKind.SEP_COMMA:
            lexer.get_next_token()
            step_exp = ExpParser.parse_exp(lexer)
        else:
            step_exp = lua_exp.IntegerExp(lexer.get_line(), 1)

        line_of_do, _ = lexer.get_next_token_of_kind(TokenKind.KW_DO)
        block = Parser.parse_block(lexer)
        lexer.get_next_token_of_kind(TokenKind.KW_END)

        return lua_stat.ForNumStat(line_of_for, line_of_do, var_name, init_exp, limit_exp, step_exp, block)

    # for Name '=' exp ',' exp [',' exp] do block end
    @staticmethod
    def finish_for_in_stat(lexer, name):
        name_list = StatParser.finish_name_list(lexer, name)
        lexer.get_next_token_of_kind(TokenKind.KW_IN)
        exp_list = ExpParser.parse_exp_list(lexer)
        line_of_do, _ = lexer.get_next_token_of_kind(TokenKind.KW_DO)
        block = Parser.parse_block(lexer)
        lexer.get_next_token_of_kind(TokenKind.KW_END)
        return lua_stat.ForInStat(line_of_do, name_list, exp_list, block)

    # namelist ::= Name {',' Name}
    @staticmethod
    def finish_name_list(lexer, name0):
        names = [name0]
        while lexer.look_ahead() == TokenKind.SEP_COMMA:
            lexer.get_next_token()
            _, name = lexer.get_next_identifier()
            names.append(name)
        return names

    #
    @staticmethod
    def parse_func_def_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_FUNCTION)
        fn_exp, has_colon = StatParser.parse_func_name(lexer)
        fd_exp = ExpParser.parse_func_def_exp(lexer)
        if has_colon:
            fd_exp.par_list.insert(0, 'self')

        return lua_stat.AssignStat(fd_exp.line, fn_exp, fd_exp)

    # local function Name funcbody
    # local namelist ['=' explist]
    @staticmethod
    def parse_local_assign_or_func_def_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_LOCAL)
        if lexer.look_ahead() == TokenKind.KW_FUNCTION:
            return StatParser.finish_local_func_def_stat(lexer)
        else:
            return StatParser.finish_local_var_decl_stat(lexer)

    """
    http: // www.lua.org / manual / 5.3 / manual.html  # 3.4.11

    function f() end = > f = function() end
    function t.a.b.c.f() end = > t.a.b.c.f = function() end
    function t.a.b.c: f() end = > t.a.b.c.f = function(self) end
    local function f() end = > local f; f = function() end

    The statement 'local function f() body end' 
    translates to 'local f; f = function() body end`
    not to `local f = function() body end`
    (This only makes a difference when the body of the function
    contains references to f.)
    """

    # local function Name funcbody
    @staticmethod
    def finish_local_func_def_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_FUNCTION)
        _, name = lexer.get_next_identifier()
        fd_exp = ExpParser.parse_func_def_exp(lexer)
        return lua_stat.LocalFuncDefStat(name, fd_exp)

    # local namelist ['=' explist]
    @staticmethod
    def finish_local_var_decl_stat(lexer):
        _, name0 = lexer.get_next_identifier()
        name_list = StatParser.finish_name_list(lexer, name0)
        exp_list = []
        if lexer.look_ahead() == TokenKind.OP_ASSIGN:
            lexer.get_next_token()
            exp_list = ExpParser.parse_exp_list(lexer)
        last_line = lexer.get_line()
        return lua_stat.LocalVarDeclStat(last_line, name_list, exp_list)

    # varlist '=' explist
    # functioncall
    @staticmethod
    def parse_assign_or_func_call_stat(lexer):
        prefix_exp = ExpParser.parse_prefix_exp(lexer)
        if isinstance(prefix_exp, lua_exp.FuncCallExp):
            return prefix_exp
        else:
            return StatParser.parse_assign_stat(lexer, prefix_exp)

    # varlist '=' explist
    @staticmethod
    def parse_assign_stat(lexer, var0):
        var_list = StatParser.finish_var_list(lexer, var0)
        lexer.get_next_token_of_kind(TokenKind.OP_ASSIGN)
        exp_list = ExpParser.parse_exp_list(lexer)
        last_line = lexer.get_line()
        return lua_stat.AssignStat(last_line, var_list, exp_list)

    # varlist ::= var {',' var}
    @staticmethod
    def finish_var_list(lexer, var0):
        var_list = [StatParser.check_var(lexer, var0)]
        while lexer.look_ahead() == TokenKind.SEP_COMMA:
            lexer.get_next_token()
            exp = ExpParser.parse_prefix_exp(lexer)
            var_list.append(StatParser.check_var(lexer, exp))
        return var_list

    # var ::= Name | prefixexp '[' exp ']' | prefixexp '.' Name
    @staticmethod
    def check_var(lexer, exp):
        if isinstance(exp, lua_exp.NameExp) or isinstance(exp, lua_exp.TableAccessExp):
            return exp
        lexer.get_next_token_of_kind(-1)
        raise Exception('unreachable!')

    # function funcname funcbody
    # funcname ::= Name {'.' Name} [':' Name]
    # funcbody ::= '(' [parlist] ')' block end
    # parlist ::= namelist [',' '...'] | '...'
    # namelist ::= Name {',' Name}
    @staticmethod
    def parse_func_def_stat(lexer):
        lexer.get_next_token_of_kind(TokenKind.KW_FUNCTION)
        fn_exp, has_colon = StatParser.parse_func_name(lexer)
        fd_exp = ExpParser.parse_func_def_exp(lexer)
        if has_colon:
            fd_exp.insert(0, 'self')
        return lua_stat.AssignStat(fd_exp.line, [fd_exp], [fn_exp])

    # funcname ::= Name {'.' Name} [':' Name]
    @staticmethod
    def parse_func_name(lexer):
        line, name = lexer.get_next_identifier()
        exp = lua_exp.NameExp(line, name)
        has_colon = False

        while lexer.look_ahead() == TokenKind.SEP_DOT:
            lexer.get_next_token()
            line, name = lexer.get_next_identifier()
            idx = lua_exp.StringExp(line, name)
            exp = lua_exp.TableAccessExp(line, exp, idx)

        if lexer.look_ahead() == TokenKind.SEP_COLON:
            lexer.get_next_token()
            line, name = lexer.get_next_identifier()
            idx = lua_exp.StringExp(line, name)
            exp = lua_exp.TableAccessExp(line, exp, idx)
            has_colon = True

        return exp, has_colon
