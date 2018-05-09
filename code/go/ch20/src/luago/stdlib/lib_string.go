package stdlib

import "fmt"
import "strings"
import . "luago/api"

var strLib = map[string]GoFunction{
	"len":      strLen,
	"rep":      strRep,
	"reverse":  strReverse,
	"lower":    strLower,
	"upper":    strUpper,
	"sub":      strSub,
	"byte":     strByte,
	"char":     strChar,
	"dump":     strDump,
	"format":   strFormat,
	"packsize": strPackSize,
	"pack":     strPack,
	"unpack":   strUnpack,
	"find":     strFind,
	"match":    strMatch,
	"gsub":     strGsub,
	"gmatch":   strGmatch,
}

func OpenStringLib(ls LuaState) int {
	ls.NewLib(strLib)
	createMetatable(ls)
	return 1
}

func createMetatable(ls LuaState) {
	ls.CreateTable(0, 1)       /* table to be metatable for strings */
	ls.PushString("dummy")     /* dummy string */
	ls.PushValue(-2)           /* copy table */
	ls.SetMetatable(-2)        /* set table as metatable for strings */
	ls.Pop(1)                  /* pop dummy string */
	ls.PushValue(-2)           /* get string library */
	ls.SetField(-2, "__index") /* metatable.__index = string */
	ls.Pop(1)                  /* pop metatable */
}

/* Basic String Functions */

// string.len (s)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.len
// lua-5.3.4/src/lstrlib.c#str_len()
func strLen(ls LuaState) int {
	s := ls.CheckString(1)
	ls.PushInteger(int64(len(s)))
	return 1
}

// string.rep (s, n [, sep])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.rep
// lua-5.3.4/src/lstrlib.c#str_rep()
func strRep(ls LuaState) int {
	s := ls.CheckString(1)
	n := ls.CheckInteger(2)
	sep := ls.OptString(3, "")

	if n <= 0 {
		ls.PushString("")
	} else if n == 1 {
		ls.PushString(s)
	} else {
		a := make([]string, n)
		for i := 0; i < int(n); i++ {
			a[i] = s
		}
		ls.PushString(strings.Join(a, sep))
	}

	return 1
}

// string.reverse (s)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.reverse
// lua-5.3.4/src/lstrlib.c#str_reverse()
func strReverse(ls LuaState) int {
	s := ls.CheckString(1)

	if strLen := len(s); strLen > 1 {
		a := make([]byte, strLen)
		for i := 0; i < strLen; i++ {
			a[i] = s[strLen-1-i]
		}
		ls.PushString(string(a))
	}

	return 1
}

// string.lower (s)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.lower
// lua-5.3.4/src/lstrlib.c#str_lower()
func strLower(ls LuaState) int {
	s := ls.CheckString(1)
	ls.PushString(strings.ToLower(s))
	return 1
}

// string.upper (s)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.upper
// lua-5.3.4/src/lstrlib.c#str_upper()
func strUpper(ls LuaState) int {
	s := ls.CheckString(1)
	ls.PushString(strings.ToUpper(s))
	return 1
}

// string.sub (s, i [, j])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.sub
// lua-5.3.4/src/lstrlib.c#str_sub()
func strSub(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := len(s)
	i := posRelat(ls.CheckInteger(2), sLen)
	j := posRelat(ls.OptInteger(3, -1), sLen)

	if i < 1 {
		i = 1
	}
	if j > sLen {
		j = sLen
	}

	if i <= j {
		ls.PushString(s[i-1 : j])
	} else {
		ls.PushString("")
	}

	return 1
}

// string.byte (s [, i [, j]])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.byte
// lua-5.3.4/src/lstrlib.c#str_byte()
func strByte(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := len(s)
	i := posRelat(ls.OptInteger(2, 1), sLen)
	j := posRelat(ls.OptInteger(3, int64(i)), sLen)

	if i < 1 {
		i = 1
	}
	if j > sLen {
		j = sLen
	}

	if i > j {
		return 0 /* empty interval; return no values */
	}
	//if (j - i >= INT_MAX) { /* arithmetic overflow? */
	//  return ls.Error2("string slice too long")
	//}

	n := j - i + 1
	ls.CheckStack2(n, "string slice too long")

	for k := 0; k < n; k++ {
		ls.PushInteger(int64(s[i+k-1]))
	}
	return n
}

// string.char (···)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.char
// lua-5.3.4/src/lstrlib.c#str_char()
func strChar(ls LuaState) int {
	nArgs := ls.GetTop()

	s := make([]byte, nArgs)
	for i := 1; i <= nArgs; i++ {
		c := ls.CheckInteger(i)
		ls.ArgCheck(int64(byte(c)) == c, i, "value out of range")
		s[i-1] = byte(c)
	}

	ls.PushString(string(s))
	return 1
}

// string.dump (function [, strip])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.dump
// lua-5.3.4/src/lstrlib.c#str_dump()
func strDump(ls LuaState) int {
	// strip := ls.ToBoolean(2)
	// ls.CheckType(1, LUA_TFUNCTION)
	// ls.SetTop(1)
	// ls.PushString(string(ls.Dump(strip)))
	// return 1
	panic("todo: strDump!")
}

/* PACK/UNPACK */

// string.packsize (fmt)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.packsize
func strPackSize(ls LuaState) int {
	fmt := ls.CheckString(1)
	if fmt == "j" {
		ls.PushInteger(8) // todo
	} else {
		panic("todo: strPackSize!")
	}
	return 1
}

// string.pack (fmt, v1, v2, ···)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.pack
func strPack(ls LuaState) int {
	panic("todo: strPack!")
}

// string.unpack (fmt, s [, pos])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.unpack
func strUnpack(ls LuaState) int {
	panic("todo: strUnpack!")
}

/* STRING FORMAT */

// string.format (formatstring, ···)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.format
func strFormat(ls LuaState) int {
	fmtStr := ls.CheckString(1)
	if len(fmtStr) <= 1 || strings.IndexByte(fmtStr, '%') < 0 {
		ls.PushString(fmtStr)
		return 1
	}

	argIdx := 1
	arr := parseFmtStr(fmtStr)
	for i, s := range arr {
		if s[0] == '%' {
			if s == "%%" {
				arr[i] = "%"
			} else {
				argIdx += 1
				arr[i] = _fmtArg(s, ls, argIdx)
			}
		}
	}

	ls.PushString(strings.Join(arr, ""))
	return 1
}

func _fmtArg(tag string, ls LuaState, argIdx int) string {
	switch tag[len(tag)-1] { // specifier
	case 'c': // character
		return string([]byte{byte(ls.ToInteger(argIdx))})
	case 'i':
		tag = tag[:len(tag)-1] + "d" // %i -> %d
		return fmt.Sprintf(tag, ls.ToInteger(argIdx))
	case 'd', 'o': // integer, octal
		return fmt.Sprintf(tag, ls.ToInteger(argIdx))
	case 'u': // unsigned integer
		tag = tag[:len(tag)-1] + "d" // %u -> %d
		return fmt.Sprintf(tag, uint(ls.ToInteger(argIdx)))
	case 'x', 'X': // hex integer
		return fmt.Sprintf(tag, uint(ls.ToInteger(argIdx)))
	case 'f': // float
		return fmt.Sprintf(tag, ls.ToNumber(argIdx))
	case 's', 'q': // string
		return fmt.Sprintf(tag, ls.ToString2(argIdx))
	default:
		panic("todo! tag=" + tag)
	}
}

/* PATTERN MATCHING */

// string.find (s, pattern [, init [, plain]])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.find
func strFind(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := len(s)
	pattern := ls.CheckString(2)
	init := posRelat(ls.OptInteger(3, 1), sLen)
	if init < 1 {
		init = 1
	} else if init > sLen+1 { /* start after string's end? */
		ls.PushNil()
		return 1
	}
	plain := ls.ToBoolean(4)

	start, end := find(s, pattern, init, plain)

	if start < 0 {
		ls.PushNil()
		return 1
	}
	ls.PushInteger(int64(start))
	ls.PushInteger(int64(end))
	return 2
}

// string.match (s, pattern [, init])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.match
func strMatch(ls LuaState) int {
	s := ls.CheckString(1)
	sLen := len(s)
	pattern := ls.CheckString(2)
	init := posRelat(ls.OptInteger(3, 1), sLen)
	if init < 1 {
		init = 1
	} else if init > sLen+1 { /* start after string's end? */
		ls.PushNil()
		return 1
	}

	captures := match(s, pattern, init)

	if captures == nil {
		ls.PushNil()
		return 1
	} else {
		for i := 0; i < len(captures); i += 2 {
			capture := s[captures[i]:captures[i+1]]
			ls.PushString(capture)
		}
		return len(captures) / 2
	}
}

// string.gsub (s, pattern, repl [, n])
// http://www.lua.org/manual/5.3/manual.html#pdf-string.gsub
func strGsub(ls LuaState) int {
	s := ls.CheckString(1)
	pattern := ls.CheckString(2)
	repl := ls.CheckString(3) // todo
	n := int(ls.OptInteger(4, -1))

	newStr, nMatches := gsub(s, pattern, repl, n)
	ls.PushString(newStr)
	ls.PushInteger(int64(nMatches))
	return 2
}

// string.gmatch (s, pattern)
// http://www.lua.org/manual/5.3/manual.html#pdf-string.gmatch
func strGmatch(ls LuaState) int {
	s := ls.CheckString(1)
	pattern := ls.CheckString(2)

	gmatchAux := func(ls LuaState) int {
		captures := match(s, pattern, 1)
		if captures != nil {
			for i := 0; i < len(captures); i += 2 {
				capture := s[captures[i]:captures[i+1]]
				ls.PushString(capture)
			}
			s = s[captures[len(captures)-1]:]
			return len(captures) / 2
		} else {
			return 0
		}
	}

	ls.PushGoFunction(gmatchAux)
	return 1
}

/* helper */

/* translate a relative string position: negative means back from end */
func posRelat(pos int64, _len int) int {
	_pos := int(pos)
	if _pos >= 0 {
		return _pos
	} else if -_pos > _len {
		return 0
	} else {
		return _len + _pos + 1
	}
}
