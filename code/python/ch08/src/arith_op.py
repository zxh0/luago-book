from enum import Enum, unique


@unique
class ArithOp(Enum):
    ADD = 0    # +
    SUB = 1    # -
    MUL = 2    # *
    MOD = 3    # %
    POW = 4    # ^
    DIV = 5    # /
    IDIV = 6   # //
    BAND = 7   # &
    BOR = 8    # |
    BXOR = 9   # ~
    SHL = 10   # <<
    SHR = 11   # >>
    UNM = 12   # -
    BNOT = 13  # ~
