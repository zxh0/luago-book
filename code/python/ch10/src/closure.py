class Closure:
    def __init__(self, proto, py_func, n):
        self.proto = proto
        self.py_func = py_func
        self.upvals = []
        if proto and len(proto.upvalues) > 0:
            self.upvals = [None] * len(proto.upvalues)
        elif py_func and n > 0:
            self.upvals = [None] * n
