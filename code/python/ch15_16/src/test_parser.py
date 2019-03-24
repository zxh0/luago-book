import sys
from parser import Parser
from lexer import Lexer


def test_parser(chunk, chunkname):
    parser = Parser()
    lexer = Lexer(chunk, chunkname)
    ast = parser.parse_block(lexer)
    print(ast)


def main(file_name):
    with open(file_name, 'r') as f:
        data = f.read()
        test_parser(data, file_name)


if __name__ == '__main__':
    if len(sys.argv) == 2:
        main(sys.argv[1])
    else:
        print('Error argument')
