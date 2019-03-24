class Upvalue:
    def __init__(self, br):
        self.instack = br.read_uint8()
        self.idx = br.read_uint8()
