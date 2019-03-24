
class Compare:
    @staticmethod
    def eq(a, b):
        if a is None:
            return b is None
        if isinstance(a, bool) or isinstance(a, str):
            return a == b
        if isinstance(a, int):
            if isinstance(b, int):
                return a == b
            elif isinstance(b, float):
                return float(a) == b
            else:
                return False
        if isinstance(a, float):
            if isinstance(b, float):
                return a == b
            elif isinstance(b, int):
                return a == float(b)
            else:
                return False

    @staticmethod
    def lt(a, b):
        if isinstance(a, str) and isinstance(b, str):
            return a < b
        if isinstance(a, int):
            if isinstance(b, int):
                return a < b
            elif isinstance(b, float):
                return float(a) < b
        if isinstance(a, float):
            if isinstance(b, float):
                return a < b
            elif isinstance(b, int):
                return a < float(b)

        raise Exception('Comparison Error')

    @staticmethod
    def le(a, b):
        if isinstance(a, str) and isinstance(b, str):
            return a <= b
        if isinstance(a, int):
            if isinstance(b, int):
                return a <= b
            elif isinstance(b, float):
                return float(a) <= b
        if isinstance(a, float):
            if isinstance(b, float):
                return a <= b
            elif isinstance(b, int):
                return a <= float(b)

        raise Exception('Comparison Error')
