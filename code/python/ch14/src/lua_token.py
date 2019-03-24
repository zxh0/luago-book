class TokenKind:
    EOF = 0,            # end-of-file
    VARARG = 2,         # ...
    SEP_SEMI = 3,       # ;
    SEP_COMMA = 4,      # ,
    SEP_DOT = 5,        # .
    SEP_COLON = 6,      # :
    SEP_LABEL = 7,      # ::
    SEP_LPAREN = 8,     # (
    SEP_RPAREN = 9,     # )
    SEP_LBRACK = 10,    # [
    SEP_RBRACK = 11,    # ]
    SEP_LCURLY = 12,    # {
    SEP_RCURLY = 13,    # }
    OP_ASSIGN = 14,     # =
    OP_MINUS = 15,      # - (sub or unm)
    OP_WAVE = 16,       # ~ (bnot or bxor)
    OP_ADD = 17,        # +
    OP_MUL = 18,        # *
    OP_DIV = 19,        # /
    OP_IDIV = 20,       # #
    OP_POW = 21,        # ^
    OP_MOD = 22,        # %
    OP_BAND = 23,       # &
    OP_BOR = 24,        # |
    OP_SHR = 25,        # >>
    OP_SHL = 26,        # <<
    OP_CONCAT = 27,     # ..
    OP_LT = 28,         # <
    OP_LE = 29,         # <=
    OP_GT = 30,         # >
    OP_GE = 31,         # >=
    OP_EQ = 32,         # ==
    OP_NE = 33,         # ~=
    OP_LEN = 34,        # #
    OP_AND = 35,        # and
    OP_OR = 36,         # or
    OP_NOT = 37,        # not
    KW_BREAK = 38,      # break
    KW_DO = 39,         # do
    KW_ELSE = 40,       # else
    KW_ELSEIF = 41,     # elseif
    KW_END = 42,        # end
    KW_FALSE = 43,      # false
    KW_FOR = 44,        # for
    KW_FUNCTION = 45,   # function
    KW_GOTO = 46,       # goto
    KW_IF = 47,         # if
    KW_IN = 48,         # in
    KW_LOCAL = 49,      # local
    KW_NIL = 50,        # nil
    KW_REPEAT = 51,     # repeat
    KW_RETURN = 52,     # ret rn
    KW_THEN = 53,       # then
    KW_TRUE = 54,       # true
    KW_UNTIL = 55,      # until
    KW_WHILE = 56,      # while
    IDENTIFIER = 57,    # identifier
    NUMBER = 58,        # number literal
    STRING = 59,        # string literal
    OP_UNM = 60,        # = TOKEN_OP_MINUS # unary minus
    OP_SUB = 61,        # = TOKEN_OP_MINUS
    OP_BNOT = 62,       # = TOKEN_OP_WAVE
    OP_BXOR = 63,       # = TOKEN_OP_WAVE


keywords = {
    "and":      TokenKind.OP_AND,
    "break":    TokenKind.KW_BREAK,
    "do":       TokenKind.KW_DO,
    "else":     TokenKind.KW_ELSE,
    "elseif":   TokenKind.KW_ELSEIF,
    "end":      TokenKind.KW_END,
    "false":    TokenKind.KW_FALSE,
    "for":      TokenKind.KW_FOR,
    "function": TokenKind.KW_FUNCTION,
    "goto":     TokenKind.KW_GOTO,
    "if":       TokenKind.KW_IF,
    "in":       TokenKind.KW_IN,
    "local":    TokenKind.KW_LOCAL,
    "nil":      TokenKind.KW_NIL,
    "not":      TokenKind.OP_NOT,
    "or":       TokenKind.OP_OR,
    "repeat":   TokenKind.KW_REPEAT,
    "return":   TokenKind.KW_RETURN,
    "then":     TokenKind.KW_THEN,
    "true":     TokenKind.KW_TRUE,
    "until":    TokenKind.KW_UNTIL,
    "while":    TokenKind.KW_WHILE,
}


single_tokens = {
    ';':        TokenKind.SEP_SEMI,
    ',':        TokenKind.SEP_COMMA,
    '(':        TokenKind.SEP_LPAREN,
    ')':        TokenKind.SEP_RPAREN,
    ']':        TokenKind.SEP_RBRACK,
    '{':        TokenKind.SEP_LCURLY,
    '}':        TokenKind.SEP_RCURLY,
    '+':        TokenKind.OP_ADD,
    '-':        TokenKind.OP_MINUS,
    '*':        TokenKind.OP_MUL,
    '^':        TokenKind.OP_POW,
    '%':        TokenKind.OP_MOD,
    '&':        TokenKind.OP_BAND,
    '|':        TokenKind.OP_BOR,
    '#':        TokenKind.OP_LEN,
}


def kind_to_category(kind):
    if kind < TokenKind.SEP_SEMI:
        return "other"
    if kind <= TokenKind.SEP_RCURLY:
        return "separator"
    if kind <= TokenKind.OP_NOT:
        return "operator"
    if kind <= TokenKind.KW_WHILE:
        return "keyword"
    if kind <= TokenKind.IDENTIFIER:
        return "identifier"
    if kind <= TokenKind.NUMBER:
        return "number"
    if kind <= TokenKind.STRING:
        return "string"
    return "other"


class Token:
    def __init__(self, line, kind, value):
        self.line = line
        self.kind = kind
        self.value = value
