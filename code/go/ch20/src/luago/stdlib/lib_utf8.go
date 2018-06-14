package stdlib

import "unicode/utf8"
import . "luago/api"

/* pattern to match a single UTF-8 character */
const UTF8PATT = "[\x00-\x7F\xC2-\xF4][\x80-\xBF]*"

const MAX_UNICODE = 0x10FFFF

var utf8Lib = map[string]GoFunction{
	"len":       utfLen,
	"offset":    utfByteOffset,
	"codepoint": utfCodePoint,
	"char":      utfChar,
	"codes":     utfIterCodes,
	/* placeholders */
	"charpattern": nil,
}

func OpenUTF8Lib(ls LuaState) int {
	ls.NewLib(utf8Lib)
	ls.PushString(UTF8PATT)
	ls.SetField(-2, "charpattern")
	return 1
}

// utf8.len (s [, i [, j]])
// http://www.lua.org/manual/5.3/manual.html#pdf-utf8.len
// lua-5.3.4/src/lutf8lib.c#utflen()
func utfLen(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := len(s)
	i := posRelat(ls.OptInteger(2, 1), sLen)
	j := posRelat(ls.OptInteger(3, -1), sLen)
	ls.ArgCheck(1 <= i && i <= sLen+1, 2,
		"initial position out of string")
	ls.ArgCheck(j <= sLen, 3,
		"final position out of string")

	if i > j {
		ls.PushInteger(0)
	} else {
		n := utf8.RuneCountInString(s[i-1 : j])
		ls.PushInteger(int64(n))
	}

	return 1
}

// utf8.offset (s, n [, i])
// http://www.lua.org/manual/5.3/manual.html#pdf-utf8.offset
func utfByteOffset(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := len(s)
	n := ls.CheckInteger(2)
	i := 1
	if n < 0 {
		i = sLen + 1
	}
	i = posRelat(ls.OptInteger(3, int64(i)), sLen)
	ls.ArgCheck(1 <= i && i <= sLen+1, 3, "position out of range")
	i--

	if n == 0 {
		/* find beginning of current byte sequence */
		for i > 0 && _isCont(s[i]) {
			i--
		}
	} else {
		if i < sLen && _isCont(s[i]) {
			ls.Error2("initial position is a continuation byte")
		}
		if n < 0 {
			for n < 0 && i > 0 { /* move back */
				for { /* find beginning of previous character */
					i--
					if !(i > 0 && _isCont(s[i])) {
						break
					}
				}
				n++
			}
		} else {
			n-- /* do not move for 1st character */
			for n > 0 && i < sLen {
				for { /* find beginning of next character */
					i++
					if i >= sLen || !_isCont(s[i]) {
						break /* (cannot pass final '\0') */
					}
				}
				n--
			}
		}
	}
	if n == 0 { /* did it find given character? */
		ls.PushInteger(int64(i + 1))
	} else { /* no such character */
		ls.PushNil()
	}
	return 1
}

// utf8.codepoint (s [, i [, j]])
// http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codepoint
// lua-5.3.4/src/lutf8lib.c#codepoint()
func utfCodePoint(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := len(s)
	i := posRelat(ls.OptInteger(2, 1), sLen)
	j := posRelat(ls.OptInteger(3, int64(i)), sLen)

	ls.ArgCheck(i >= 1, 2, "out of range")
	ls.ArgCheck(int(j) <= sLen, 3, "out of range")
	if i > j {
		return 0 /* empty interval; return no values */
	}
	if j-i >= LUA_MAXINTEGER { /* (lua_Integer -> int) overflow? */
		return ls.Error2("string slice too long")
	}
	n := j - i + 1
	ls.CheckStack2(n, "string slice too long")

	n = 0
	s = s[i-1:]
	for i <= j {
		code, size := utf8.DecodeRuneInString(s)
		if code == utf8.RuneError {
			return ls.Error2("invalid UTF-8 code")
		}
		ls.PushInteger(int64(code))
		n++
		i += size
		s = s[size:]
	}
	return n
}

// utf8.char (···)
// http://www.lua.org/manual/5.3/manual.html#pdf-utf8.char
// lua-5.3.4/src/lutf8lib.c#utfchar()
func utfChar(ls LuaState) int {
	n := ls.GetTop() /* number of arguments */
	codePoints := make([]rune, n)

	for i := 1; i <= n; i++ {
		cp := ls.CheckInteger(i)
		ls.ArgCheck(0 <= cp && cp <= MAX_UNICODE, i, "value out of range")
		codePoints[i-1] = rune(cp)
	}

	ls.PushString(_encodeUtf8(codePoints))
	return 1
}

func _encodeUtf8(codePoints []rune) string {
	buf := make([]byte, 6)
	str := make([]byte, 0, len(codePoints))

	for _, cp := range codePoints {
		n := utf8.EncodeRune(buf, cp)
		str = append(str, buf[0:n]...)
	}

	return string(str)
}

// utf8.codes (s)
// http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codes
func utfIterCodes(ls LuaState) int {
	ls.CheckString(1)
	ls.PushGoFunction(_iterAux)
	ls.PushValue(1)
	ls.PushInteger(0)
	return 3
}

func _iterAux(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := int64(len(s))
	n := ls.ToInteger(2) - 1
	if n < 0 { /* first iteration? */
		n = 0 /* start from here */
	} else if n < sLen {
		n++ /* skip current byte */
		for n < sLen && _isCont(s[n]) {
			n++
		} /* and its continuations */
	}
	if n >= sLen {
		return 0 /* no more codepoints */
	} else {
		code, _ := utf8.DecodeRuneInString(s[n:])
		if code == utf8.RuneError {
			return ls.Error2("invalid UTF-8 code")
		}
		ls.PushInteger(n + 1)
		ls.PushInteger(int64(code))
		return 2
	}
}

func _isCont(b byte) bool {
	return b&0xC0 == 0x80
}
