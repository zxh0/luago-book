from binary_reader import BinaryReader
from binary_chunk_header import BinaryChunkHeader
from prototype import Prototype


class BinaryChunk:
    def __init__(self, chunk):
        self.binary_reader = BinaryReader(chunk)
        self.header = None
        self.size_upvalues = None
        self.main_func = None

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
        self.main_func = Prototype(self.binary_reader, '')
        return self.main_func
