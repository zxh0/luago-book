class LocalVar:
    def __init__(self, br):
        self.var_name = br.read_lua_str()
        self.start_pc = br.read_uint32()
        self.end_pc = br.read_uint32()
