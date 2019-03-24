from bin_chunk.binary_reader import BinaryReader
from bin_chunk.binary_chunk_header import BinaryChunkHeader
from vm.prototype import Prototype


class BinaryChunk:
    def __init__(self, chunk):
        self.binary_reader = BinaryReader(chunk)
        self.header = None
        self.size_upvalues = None
        self.main_func = None

    @staticmethod
    def is_binary_chunk(data):
        if data is None or len(data) < 4:
            return False

        for i in range(4):
            if data[i] != BinaryChunkHeader.LUA_SIGNATURE[i]:
                return False

        return True

    def print_header(self):
        self.header.dump()

    def check_header(self):
        self.header.check()

    def print_main_func(self):
        self.main_func.dump()

    def get_main_func(self):
        return self.main_func

    def undump(self):
        self.header = BinaryChunkHeader(self.binary_reader)
        self.check_header()
        self.size_upvalues = self.binary_reader.read_uint8()
        self.main_func = Prototype()
        self.main_func.init_from_br(self.binary_reader, '')
        return self.main_func
