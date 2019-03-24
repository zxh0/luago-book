from enum import Enum, unique


@unique
class CmpOp(Enum):
    EQ = 0    # ==
    LT = 1    # <
    LE = 2    # <=
