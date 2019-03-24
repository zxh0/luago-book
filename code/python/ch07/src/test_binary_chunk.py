from binary_chunk import BinaryChunk


def main():
    bc = BinaryChunk('../test/hello.luac')
    bc.print_header()
    bc.check_header()
    bc.print_main_func()


if __name__ == '__main__':
    main()
