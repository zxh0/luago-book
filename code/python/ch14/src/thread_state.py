from enum import Enum, unique


@unique
class ThreadStatus(Enum):
    OK = 0
    YIELD = 1
    ERRRUN = 2
    ERRSYNTAX = 3
    ERRMEM = 4
    ERRGCMM = 5
    ERRERR = 6
    ERRFILE = 7
