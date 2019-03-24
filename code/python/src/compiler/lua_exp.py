"""
exp ::=  nil | false | true | Numeral | LiteralString | ‘...’ | functiondef |
    prefixexp | tableconstructor | exp binop exp | unop exp
prefixexp ::= var | functioncall | ‘(’ exp ‘)’
var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
functioncall ::=  prefixexp args | prefixexp ‘:’ Name args
"""


# nil
class NilExp:
    def __init__(self, line):
        self.line = line


# true
class TrueExp:
    def __init__(self, line):
        self.line = line


# false
class FalseExp:
    def __init__(self, line):
        self.line = line


# ...
class VarArgExp:
    def __init__(self, line):
        self.line = line


# Numeral: integer
class IntegerExp:
    def __init__(self, line, val):
        self.line = line
        self.val = val

    def __str__(self):
        s = ''
        s += '"Line": ' + str(self.line) + '\n'
        s += '"Val": ' + str(self.val) + '\n'
        return s


# Numeral: float
class FloatExp:
    def __init__(self, line, val):
        self.line = line
        self.val = val


# Literal String
class StringExp:
    def __init__(self, line, s):
        self.line = line
        self.s = s

    def __str__(self):
        return '"Line": ' + str(self.line) + '\n' + '"Str": ' + '"' + self.s + '"'


# unop exp
class UnopExp:
    def __init__(self, line, op, exp):
        self.line = line
        self.op = op
        self.exp = exp

    def __str__(self):
        s = ''
        s += 'Line: ' + str(self.line) + '\n'
        s += 'Op: ' + str(self.op) + '\n'
        s += 'Exp: ' + '\n'
        for l in str(self.exp).split('\n'):
            if len(l):
                s += '  ' + l + '\n'
        return s


# exp1 op exp2
class BinopExp:
    def __init__(self, line, op, exp1, exp2):
        self.line = line
        self.op = op
        self.exp1 = exp1
        self.exp2 = exp2

    def __str__(self):
        s = '"Line": ' + str(self.line) + '\n'
        s += '"Op": ' + str(self.op) + '\n'
        s += '"exp1": ' '\n'
        for l in str(self.exp1).split('\n'):
            if len(l) > 0:
                s += '  ' + l + '\n'

        s += '"exp2": ' '\n'
        for l in str(self.exp2).split('\n'):
            if len(l) > 0:
                s += '  ' + l + '\n'
        return s


# ..
class ConcatExp:
    def __init__(self, line, exps):
        self.line = line
        self.exps = exps


# tableconstructor ::= '{' [fieldlist] ''}
# fieldlist ::= field {fieldsep field} [fieldsep]
# field ::= '[' exp ']' '=' exp | Name '=' exp | exp
# fieldsep ::= ',' | ';'
class TableConstructorExp:
    def __init__(self, line, last_line, key_exps, val_exps):
        self.line = line
        self.last_line = last_line
        self.key_exps = key_exps
        self.val_exps = val_exps


# functiondef ::= function funcbody
# funcbody ::= '(' [parlist] ')' block end
# parlist ::= namelist [',' '...'] | '...'
# namelist ::= Name {',' Name}
class FuncDefExp:
    def __init__(self, line, last_line, par_list, is_var_arg, block):
        self.line = line
        self.last_line = last_line
        self.par_list = par_list
        self.is_var_arg = is_var_arg
        self.block = block

    def __str__(self):
        s = ''
        s += 'Line: ' + str(self.line) + '\n'
        s += 'LastLine: ' + str(self.last_line) + '\n'
        s += 'ParList: ' + '\n'
        if self.par_list is not None:
            for par in self.par_list:
                for l in str(par).split('\n'):
                    if len(l):
                        s += '  ' + l + '\n'
        s += 'IsVarArg: ' + str(self.is_var_arg) + '\n'
        s += 'Block: '
        for l in str(self.block).split('\n'):
            if len(l):
                s += '  ' + l + '\n'
        return s


"""
prefixexp ::= Name |
              ‘(’ exp ‘)’ |
              prefixexp ‘[’ exp ‘]’ |
              prefixexp ‘.’ Name |
              prefixexp ‘:’ Name args |
              prefixexp args
"""


class NameExp:
    def __init__(self, line, name):
        self.line = line
        self.name = name

    def __str__(self):
        return '"Line": ' + str(self.line) + '\n' + '"Name": ' + '"' + self.name + '"'


class ParensExp:
    def __init__(self, exp):
        self.exp = exp


class TableAccessExp:
    def __init__(self, last_line, prefix_exp, key_exp):
        self.last_line = last_line
        self.prefix_exp = prefix_exp
        self.key_exp = key_exp


class FuncCallExp:
    def __init__(self, line, last_line, prefix_exp, name_exp, args):
        self.line = line
        self.last_line = last_line
        self.prefix_exp = prefix_exp
        self.name_exp = name_exp
        self.args = args

    def __str__(self):
        s = ''
        s += '"Line": ' + str(self.line) + ',\n'
        s += '"LastLine": ' + str(self.last_line) + ',\n'
        s += '"PrefixExp": {\n'
        for line in str(self.prefix_exp).split('\n'):
            s += '  ' + line + '\n'
        s += '},\n'
        s += '"NameExp": ' + str(self.name_exp) + ',\n'
        s += '"Args": ' + '['
        for arg in self.args:
            s += '{\n'
            for line in str(arg).split('\n'):
                if len(line):
                    s += '  ' + line + '\n'
            s += '}'
        s += ']'
        return s


class ExpHelper:
    @staticmethod
    def is_vararg_or_func_call(exp):
        return isinstance(exp, VarArgExp) or isinstance(exp, FuncCallExp)

    @staticmethod
    def remove_tail_nils(exps):
        while len(exps) > 0:
            if isinstance(exps[-1], NilExp):
                exps = exps[:-1]
            else:
                break

        return exps
