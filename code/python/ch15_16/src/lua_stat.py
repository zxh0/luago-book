"""
stat ::=  ‘;’ |
    varlist ‘=’ explist |
    functioncall |
    label |
    break |
    goto Name |
    do block end |
    while exp do block end |
    repeat block until exp |
    if exp then block {elseif exp then block} [else block] end |
    for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end |
    for namelist in explist do block end |
    function funcname funcbody |
    local function Name funcbody |
    local namelist [‘=’ explist]
"""


# ';'
class EmptyStat:
    def __init__(self, line):
        self.line = line


# break
class BreakStat:
    def __init__(self, line):
        self.line = line


# '::' Name '::'
class LabelStat:
    def __init__(self, name):
        self.name = name


# goto Name
class GotoStat:
    def __init__(self, name):
        self.name = name


# do block end
class DoStat:
    def __init__(self, block):
        self.block = block


# functioncall
class FuncCallStat:
    def __init__(self, line):
        self.line = line


# if exp then block {elseif exp then block} [else block] end
class IfStat:
    def __init__(self, exps, blocks):
        self.exps = exps
        self.blocks = blocks

    def __str__(self):
        s = ''
        s += '"Exps": ' + '\n'
        for exp in self.exps:
            for l in str(exp).split('\n'):
                s += '  ' + l + '\n'

        s += '"Blocks": ' + '\n'
        for block in self.blocks:
            for l in str(block).split('\n'):
                s += '  ' + l + '\n'

        return s


# while exp do block end
class WhileStat:
    def __init__(self, exp, block):
        self.exp = exp
        self.block = block

    def __str__(self):
        s = '"While": \n'
        s += '  "Exp": \n'
        for l in str(self.exp).split('\n'):
            if len(l):
                s += '    ' + l + '\n'
        s += '  "Block": \n'
        for l in str(self.block).split('\n'):
            if len(l):
                s += '    ' + l + '\n'
        return s


# repeat block until exp
class RepeatStat:
    def __init__(self, block, exp):
        self.block = block
        self.exp = exp

    def __str__(self):
        s = '"Repeat": \n'
        s += '  "Block": \n'
        for l in str(self.block).split('\n'):
            if len(l):
                s += '    ' + l + '\n'
        s += '  "Exp": \n'
        for l in str(self.exp).split('\n'):
            if len(l):
                s += '    ' + l + '\n'
        return s


# for Name '=' exp ',' exp [',' exp] do block end
class ForNumStat:
    def __init__(self, line_of_for, line_of_do, var_name, init_exp, limit_exp, step_exp, block):
        self.line_of_for = line_of_for
        self.line_of_do = line_of_do
        self.var_name = var_name
        self.init_exp = init_exp
        self.limit_exp = limit_exp
        self.step_exp = step_exp
        self.block = block

    def __str__(self):
        s = ''
        s += 'Line of for: ' + str(self.line_of_for) + '\n'
        s += 'Line of do: ' + str(self.line_of_do) + '\n'
        s += 'Var name: ' + str(self.var_name) + '\n'
        s += 'Init exp: ' + '\n'
        for l in str(self.init_exp).split('\n'):
            s += '  ' + l + '\n'
        s += 'Limit exp: ' + '\n'
        for l in str(self.limit_exp).split('\n'):
            s += '  ' + l + '\n'
        s += 'Step exp: ' + '\n'
        for l in str(self.step_exp).split('\n'):
            s += '  ' + l + '\n'
        s += 'Block: ' + '\n'
        for l in str(self.block).split('\n'):
            if len(l):
                s += '  ' + l + '\n'

        return s


# for namelist in explist do block end
# namelist ::= Name {',' Name}
# explist ::= exp {',' exp}
class ForInStat:
    def __init__(self, line_of_do, name_list, exp_list, block):
        self.line_of_do = line_of_do
        self.name_list = name_list
        self.exp_list = exp_list
        self.block = block

    def __str__(self):
        s = ''
        s += 'Line of do: ' + str(self.line_of_do) + '\n'
        for name in self.name_list:
            s += '  ' + str(name)
        for exp in self.exp_list:
            for l in str(exp).split('\n'):
                if len(l):
                    s += '  ' + l + '\n'
        for l in str(self.block).split('\n'):
            if len(l):
                s += '  ' + l + '\n'
        return s


# varlist '=' explist
# varlist ::= var {',' var}
# var ::= Name | prefixexp '[' exp ']' | prefixexp '.' Name
class AssignStat:
    def __init__(self, last_line, var_list, exp_list):
        self.last_line = last_line
        self.var_list = var_list
        self.exp_list = exp_list

    def __str__(self):
        s = ''
        s += '"LastLine": ' + str(self.last_line) + '\n'
        s += '"VarList": ' + '\n'
        for var in self.var_list:
            for l in str(var).split('\n'):
                if len(l):
                    s += '  ' + l + '\n'

        s += '"ExpList": ' + '\n'
        for exp in self.exp_list:
            for l in str(exp).split('\n'):
                if len(l):
                    s += '  ' + l + '\n'

        return s


# local namelist ['=' explist]
# namelist ::= Name {',' Name}
# explist ::= exp {',' exp}
class LocalVarDeclStat:
    def __init__(self, last_line, name_list, exp_list):
        self.last_line = last_line
        self.name_list = name_list
        self.exp_list = exp_list


# local function Name funcbody
class LocalFuncDefStat:
    def __init__(self, name, exp):
        self.name = name
        self.exp = exp
