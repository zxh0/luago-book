package stdlib

import "regexp"
import "strings"

// tag = %[flags][width][.precision]specifier
var tagPattern = regexp.MustCompile(`%[ #+-0]?[0-9]*(\.[0-9]+)?[cdeEfgGioqsuxX%]`)

func parseFmtStr(fmt string) []string {
	if fmt == "" || strings.IndexByte(fmt, '%') < 0 {
		return []string{fmt}
	}

	parsed := make([]string, 0, len(fmt)/2)
	for {
		if fmt == "" {
			break
		}

		loc := tagPattern.FindStringIndex(fmt)
		if loc == nil {
			parsed = append(parsed, fmt)
			break
		}

		head := fmt[:loc[0]]
		tag := fmt[loc[0]:loc[1]]
		tail := fmt[loc[1]:]

		if head != "" {
			parsed = append(parsed, head)
		}
		parsed = append(parsed, tag)
		fmt = tail
	}
	return parsed
}

func find(s, pattern string, init int, plain bool) (start, end int) {
	tail := s
	if init > 1 {
		tail = s[init-1:]
	}

	if plain {
		start = strings.Index(tail, pattern)
		end = start + len(pattern) - 1
	} else {
		re, err := _compile(pattern)
		if err != "" {
			panic(err) // todo
		} else {
			loc := re.FindStringIndex(tail)
			if loc == nil {
				start, end = -1, -1
			} else {
				start, end = loc[0], loc[1]-1
			}
		}
	}
	if start >= 0 {
		start += len(s) - len(tail) + 1
		end += len(s) - len(tail) + 1
	}

	return
}

func match(s, pattern string, init int) []int {
	tail := s
	if init > 1 {
		tail = s[init-1:]
	}

	re, err := _compile(pattern)
	if err != "" {
		panic(err) // todo
	} else {
		found := re.FindStringSubmatchIndex(tail)
		if len(found) > 2 {
			return found[2:]
		} else {
			return found
		}
	}
}

// todo
func gsub(s, pattern, repl string, n int) (string, int) {
	re, err := _compile(pattern)
	if err != "" {
		panic(err) // todo
	} else {
		indexes := re.FindAllStringIndex(s, n)
		if indexes == nil {
			return s, 0
		}

		nMatches := len(indexes)
		lastEnd := indexes[nMatches-1][1]
		head, tail := s[:lastEnd], s[lastEnd:]

		newHead := re.ReplaceAllString(head, repl)
		return newHead + tail, nMatches
	}
}

func _compile(pattern string) (*regexp.Regexp, string) {
	re, err := regexp.Compile(pattern)
	if err != nil {
		return nil, err.Error() // todo
	} else {
		return re, ""
	}
}
