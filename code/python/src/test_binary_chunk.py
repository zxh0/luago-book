from bin_chunk.binary_chunk import BinaryChunk


def main():
    with open('./lua/hello.luac', 'rb') as f:
        chunk = f.read()
        if BinaryChunk.is_binary_chunk(chunk):
            bc = BinaryChunk(chunk)
            bc.undump()
            bc.print_header()
            bc.check_header()
            bc.print_main_func()


if __name__ == '__main__':
    main()
