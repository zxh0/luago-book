from compiler.lua_token import *
import re


class Lexer:
    re_new_line = re.compile(r"\r\n|\n\r|\n|\r")
    re_identifier = re.compile(r"^[_\d\w]+")
    re_opening_long_bracket = re.compile(r"^\[=*\[")
    re_short_string = re.compile(r"(?s)(^'(\\\\|\\'|\\\n|\\z\s*|[^'\n])*')|(^\"(\\\\|\\\"|\\\n|\\z\s*|[^\"\n])*\")")
    re_number = re.compile(r"^0[xX][0-9a-fA-F]*(\.[0-9a-fA-F]*)?([pP][+\-]?[0-9]+)?|"
                           r"^[0-9]*(\.[0-9]*)?([eE][+\-]?[0-9]+)?")

    re_dec_escape_seq = re.compile(r"^\\[0-9]{1,3}")
    re_hex_escape_seq = re.compile(r"^\\x[0-9a-fA-F]{2}")
    re_unicode_escape_seq = re.compile(r"^\\u{[0-9a-fA-F]+}")

    def __init__(self, chunk, chunk_name):
        self.chunk = chunk
        self.chunk_name = chunk_name
        self.line = 1
        self.next_token = None
        self.next_token_kind = None
        self.next_token_line = 0

    def get_line(self):
        return self.line

    def get_next_token_of_kind(self, k):
        line, kind, token = self.get_next_token()
        if k != kind:
            err = 'unexpected symbol near "{0}"'.format(token)
            self.error(err)
        return line, token

    def get_next_identifier(self):
        return self.get_next_token_of_kind(TokenKind.IDENTIFIER)

    def look_ahead(self):
        if self.next_token_line > 0:
            return self.next_token_kind
        current_line = self.line
        line, kind, token = self.get_next_token()
        self.line = current_line
        self.next_token_kind = kind
        self.next_token_line = line
        self.next_token = token
        return kind

    def get_next_token(self):
        if self.next_token_line > 0:
            line, kind, token = self.next_token_line, self.next_token_kind, self.next_token
            self.line = self.next_token_line
            self.next_token_line = 0
            return line, kind, token

        self.skip_space()
        if len(self.chunk) == 0:
            return self.line, TokenKind.EOF, 'EOF'

        c = self.chunk[0]
        if c in single_tokens:
            self.next(1)
            return self.line, single_tokens[c], c
        if c == ':':
            if self.test("::"):
                self.next(2)
                return self.line, TokenKind.SEP_LABEL, '::'
            else:
                self.next(1)
                return self.line, TokenKind.SEP_COLON, c
        if c == '/':
            if self.test('//'):
                self.next(2)
                return self.line, TokenKind.OP_IDIV, '//'
            else:
                self.next(1)
                return self.line, TokenKind.OP_DIV, c
        if c == '~':
            if self.test('~='):
                self.next(2)
                return self.line, TokenKind.OP_NE, '~='
            else:
                self.next(1)
                return self.line, TokenKind.OP_WAVE, c
        if c == '=':
            if self.test('=='):
                self.next(2)
                return self.line, TokenKind.OP_EQ, '=='
            else:
                self.next(1)
                return self.line, TokenKind.OP_ASSIGN, c
        if c == '<':
            if self.test('<<'):
                self.next(2)
                return self.line, TokenKind.OP_SHL, '<<'
            elif self.test('<='):
                self.next(2)
                return self.line, TokenKind.OP_LE, '<='
            else:
                self.next(1)
                return self.line, TokenKind.OP_LT, c
        if c == '>':
            if self.test('>>'):
                self.next(2)
                return self.line, TokenKind.OP_SHR, '>>'
            elif self.test('>='):
                self.next(2)
                return self.line, TokenKind.OP_GE, '>='
            else:
                self.next(1)
                return self.line, TokenKind.OP_GT, c
        if c == '.':
            if self.test('...'):
                self.next(3)
                return self.line, TokenKind.VARARG, '...'
            elif self.test('..'):
                self.next(2)
                return self.line, TokenKind.OP_CONCAT, '..'
            elif len(self.chunk) == 1 or not self.chunk[1].isdigit():
                self.next(1)
                return self.line, TokenKind.SEP_DOT, c
        if c == '[':
            if self.test('[[') or self.test('[='):
                return self.line, TokenKind.STRING, self.scan_long_string()
            else:
                self.next(1)
                return self.line, TokenKind.SEP_LBRACK, '['
        if c in ('\'', '"'):
            return self.line, TokenKind.STRING, self.scan_short_string()

        if c == '.' or c.isdigit():
            token = self.scan_number()
            return self.line, TokenKind.NUMBER, token
        if c == '_' or c.isalpha():
            token = self.scan_identifier()
            if token in keywords:
                return self.line, keywords[token], token
            else:
                return self.line, TokenKind.IDENTIFIER, token

        err = 'unexpected symbol near "{0}"'.format(c)
        self.error(err)

    def next(self, n):
        self.chunk = self.chunk[n:]

    def test(self, s):
        return self.chunk.startswith(s)

    def error(self, f, *args):
        err = f.format(args)
        err = '{0}:{1}: {2}'.format(self.chunk_name, self.line, err)
        raise Exception(err)

    def skip_space(self):
        while len(self.chunk) > 0:
            if self.test('--'):
                self.skip_comment()
            elif self.test('\r\n') or self.test('\n\r'):
                self.next(2)
                self.line += 1
            elif Lexer.is_new_line(self.chunk[0]):
                self.next(1)
                self.line += 1
            elif Lexer.is_white_space(self.chunk[0]):
                self.next(1)
            else:
                break

    @staticmethod
    def is_white_space(c):
        return c in ('\t', '\n', '\v', '\f', '\r', ' ')

    @staticmethod
    def is_new_line(c):
        return c in ('\r', '\n')

    def skip_comment(self):
        self.next(2)

        if self.test('['):
            if re.match(Lexer.re_opening_long_bracket, self.chunk):
                self.scan_long_string()
                return

        while len(self.chunk) > 0 and not self.is_new_line(self.chunk[0]):
            self.next(1)

    def scan_identifier(self):
        return self.scan(Lexer.re_identifier)

    def scan_short_string(self):
        m = re.match(Lexer.re_short_string, self.chunk)
        if m:
            s = m.group()
            self.next(len(s))
            s = s[1: len(s)-1]
            if s.find('\\') >= 0:
                result = Lexer.re_new_line.findall(s)
                self.line += len(result)
                s = self.escape(s)
            return s
        self.error('unfinished string')
        return ''

    def scan_long_string(self):
        m_open = re.match(Lexer.re_opening_long_bracket, self.chunk)
        if m_open is None:
            self.error('invalid long string delimiter near "%s"'.format(self.chunk[0:2]))

        str_open = m_open.group()
        str_close = str_open.replace('[', ']')
        close_idx = self.chunk.find(str_close)
        if close_idx < 0:
            self.error('unfinished long string or comment')

        s = self.chunk[len(str_open): close_idx]
        self.next(close_idx + len(str_close))

        s = re.sub(Lexer.re_new_line, s, '\n')
        self.line += s.count('\n')
        if len(s) > 0 and s[0] == '\n':
            s = s[1:]

        return s

    def scan_number(self):
        return self.scan(Lexer.re_number)

    def scan(self, pattern):
        m = re.match(pattern, self.chunk)
        if m:
            token = m.group()
            self.next(len(token))
            return token
        raise Exception('unreachable')

    def escape(self, s):
        ret = ''
        while len(s) > 0:
            if s[0] != '\\':
                ret += s[0]
                s = s[1:]
                continue

            if len(s) == 1:
                self.error('unfinished string')

            if s[1] == 'a':
                ret += '\a'
                s = s[2:]
                continue
            elif s[1] == 'b':
                ret += '\b'
                s = s[2:]
                continue
            elif s[1] == 'f':
                ret += '\f'
                s = s[2:]
                continue
            elif s[1] == 'n' or s[1] == '\n':
                ret += '\n'
                s = s[2:]
                continue
            elif s[1] == 'r':
                ret += '\r'
                s = s[2:]
                continue
            elif s[1] == 't':
                ret += '\t'
                s = s[2:]
                continue
            elif s[1] == 'v':
                ret += '\v'
                s = s[2:]
                continue
            elif s[1] == '"':
                ret += '"'
                s = s[2:]
                continue
            elif s[1] == '\'':
                ret += '\''
                s = s[2:]
                continue
            elif s[1] == '\\':
                ret += '\\'
                s = s[2:]
                continue
            elif s[1] in '0123456789':
                m = re.match(Lexer.re_dec_escape_seq, s)
                if m:
                    str_dec = m.group()[1:]
                    d = int(str_dec)
                    if d <= 0xff:
                        ret += str(chr(d))
                        s = s[len(m.group()):]
                        continue
                    self.error('decimal escape too large near "%s"'.format(m.group()))
            elif s[1] == 'x':
                m = re.match(Lexer.re_hex_escape_seq, s)
                if m:
                    str_hex = '0' + m.group()[1:]
                    d = int(str_hex, 16)
                    ret += str(chr(d))
                    s = s[len(m.group()):]
                    continue
            elif s[1] == 'u':
                m = re.match(Lexer.re_unicode_escape_seq, s)
                if m:
                    str_unicode = m.group()[3: len(m.group())-1]
                    d = int(str_unicode, 16)
                    if d <= 0x10ffff:
                        ret += str(chr(d))
                        s = s[len(m.group()):]
                        continue
                    self.error('UTF-8 value too large near "%s"'.format(str_unicode))
            elif s[1] == 'z':
                s = s[2:]
                while len(s) > 0 and Lexer.is_white_space(s[0]):
                    s = s[1:]
                continue
        return ret
