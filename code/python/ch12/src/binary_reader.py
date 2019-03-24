import struct


class BinaryReader:
    def __init__(self, data):
        self.data = data

    def read_bytes(self, byte_num):
        ret = self.data[: byte_num]
        self.data = self.data[byte_num:]
        return ret

    def read_uint8(self):
        ret, = struct.unpack('b', self.read_bytes(1))
        return ret

    def read_uint16(self):
        ret, = struct.unpack('H', self.read_bytes(2))
        return ret

    def read_uint32(self):
        ret, = struct.unpack('I', self.read_bytes(4))
        return ret

    def read_uint64(self):
        ret, = struct.unpack('Q', self.read_bytes(8))
        return ret

    def read_float(self):
        ret, = struct.unpack('f', self.read_bytes(4))
        return ret

    def read_double(self):
        ret, = struct.unpack('d', self.read_bytes(8))
        return ret

    def read_lua_str(self):
        size = self.read_uint8()
        if size == 0:
            return None
        if size == 0xff:
            size = self.read_uint8()

        bs = self.read_bytes(size-1)
        return str(bs, encoding='utf-8')

    def read_lua_int(self):
        ret, = struct.unpack('q', self.read_bytes(8))
        return ret

    def read_lua_number(self):
        return self.read_double()
