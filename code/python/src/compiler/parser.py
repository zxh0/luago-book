from compiler.lua_token import TokenKind
from compiler.exp_parser import ExpParser


class Block:
    # block ::= {stat} [retstat]
    def __init__(self, lexer):
        self.stats = Block.parse_stats(lexer)
        self.ret_exps = Block.parse_ret_exps(lexer)
        self.last_line = lexer.get_line()

    def __str__(self):
        s = '"LastLine": ' + str(self.last_line) + ',\n'
        s += '"Stats": ' + '['
        for stat in self.stats:
            s += '{\n'
            for line in str(stat).split('\n'):
                if len(line):
                    s += '  ' + line + '\n'
            s += '}'
        s += ']\n'
        s += '"RetExps": ' + '\n'
        if self.ret_exps:
            for exp in self.ret_exps:
                for l in str(exp).split('\n'):
                    if len(l):
                        s += '  ' + l + '\n'
        else:
            s += '  nil'
        return s

    @staticmethod
    def parse_stats(lexer):
        from compiler.stat_parser import StatParser
        stats = []
        while not TokenKind.is_return_or_block_end(lexer.look_ahead()):
            stat = StatParser.parse_stat(lexer)
            if stat is not None:
                stats.append(stat)
        return stats

    # retstat ::= return [explist] [';']
    # explist ::= exp {',' exp}
    @staticmethod
    def parse_ret_exps(lexer):
        if lexer.look_ahead() != TokenKind.KW_RETURN:
            return None

        lexer.get_next_token()
        kind = lexer.look_ahead()
        if kind in (TokenKind.EOF, TokenKind.KW_END, TokenKind.KW_ELSE, TokenKind.KW_ELSEIF, TokenKind.KW_UNTIL):
            return []
        if kind == TokenKind.SEP_SEMI:
            lexer.get_next_token()
            return []

        exps = ExpParser.parse_exp_list(lexer)
        if lexer.look_ahead() == TokenKind.SEP_SEMI:
            lexer.get_next_token()
        return exps


class Parser:
    def __init__(self):
        pass

    @staticmethod
    def parse_block(lexer):
        return Block(lexer)
