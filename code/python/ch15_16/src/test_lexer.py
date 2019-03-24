import sys
from lexer import Lexer
from lua_token import *


def main():
    with open(sys.argv[1], 'r') as f:
        data = f.read()
        lexer = Lexer(data, sys.argv[1])
        while True:
            try:
                line, kind, token = lexer.get_next_token()
                print('[%2d] [%-10s] %s' % (line, kind_to_category(kind), token))
                if kind == TokenKind.EOF:
                    break
            except Exception as e:
                sys.exit(e)


if __name__ == '__main__':
    if len(sys.argv) == 2:
        main()
    else:
        print('Error argument')
