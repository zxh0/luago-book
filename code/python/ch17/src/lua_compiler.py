from parser import Parser
from lexer import Lexer
from codegen import Codegen


class LuaCompiler:
    @staticmethod
    def compile(chunk, chunk_name):
        parser = Parser()
        lexer = Lexer(chunk, chunk_name)
        ast = parser.parse_block(lexer)
        # print(ast)
        proto = Codegen.gen_proto(ast)
        # proto.print_code()
        LuaCompiler.set_source(proto, chunk_name)
        return proto

    @staticmethod
    def set_source(proto, chunk_name):
        proto.source = chunk_name
        for sub in proto.get_protos():
            LuaCompiler.set_source(sub, chunk_name)
