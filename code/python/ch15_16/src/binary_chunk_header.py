class BinaryChunkHeader:
    LUA_SIGNATURE = bytes(b'\x1bLua')
    LUAC_VERSION = 0x53
    LUAC_FORMAT = 0x0
    LUAC_DATA = bytes(b'\x19\x93\r\n\x1a\n')
    CINT_SIZE = 4
    CSIZET_SIZE = 8
    INST_SIZE = 4
    LUA_INT_SIZE = 8
    LUA_NUMBER_SIZE = 8
    LUAC_INT = 0x5678
    LUAC_NUM = 370.5

    def __init__(self, br):
        self.signature = br.read_bytes(4)
        self.version = br.read_uint8()
        self.format = br.read_uint8()
        self.luac_data = br.read_bytes(6)
        self.cint_size = br.read_uint8()
        self.csizet_size = br.read_uint8()
        self.inst_size = br.read_uint8()
        self.lua_int_size = br.read_uint8()
        self.lua_number_size = br.read_uint8()
        self.luac_int = br.read_uint64()
        self.luac_num = br.read_double()

    def check(self):
        assert(self.signature == BinaryChunkHeader.LUA_SIGNATURE)
        assert(self.version == BinaryChunkHeader.LUAC_VERSION)
        assert(self.format == BinaryChunkHeader.LUAC_FORMAT)
        assert(self.luac_data == BinaryChunkHeader.LUAC_DATA)
        assert(self.cint_size == BinaryChunkHeader.CINT_SIZE)
        assert(self.csizet_size == BinaryChunkHeader.CSIZET_SIZE)
        assert(self.inst_size == BinaryChunkHeader.INST_SIZE)
        assert(self.lua_int_size == BinaryChunkHeader.LUA_INT_SIZE)
        assert(self.lua_number_size == BinaryChunkHeader.LUA_NUMBER_SIZE)
        assert(self.luac_int == BinaryChunkHeader.LUAC_INT)
        assert(self.luac_num == BinaryChunkHeader.LUAC_NUM)

    def dump(self):
        print('signature:       ', self.signature)
        print('version:         ', self.version)
        print('format:          ', self.format)
        print('luac_data:       ', self.luac_data)
        print('cint_size:       ', self.cint_size)
        print('csizet_size:     ', self.csizet_size)
        print('inst_size:       ', self.inst_size)
        print('lua_int_size:    ', self.lua_int_size)
        print('lua_number_size: ', self.lua_number_size)
        print('luac_int:        ', hex(self.luac_int))
        print('luac_num:        ', self.luac_num)
        print()
