class Upvalue:
    def __init__(self, instack, idx):
        self.instack = instack
        self.idx = idx

    def get_idx(self):
        return self.idx

    def get_in_stack(self):
        return self.instack
