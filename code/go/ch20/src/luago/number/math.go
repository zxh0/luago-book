package number

import "math"

// todo: correct?
func FloatToInteger(f float64) (int64, bool) {
	i := int64(f)
	return i, float64(i) == f
}

// a % b == a - ((a // b) * b)
func IMod(a, b int64) int64 {
	return a - IFloorDiv(a, b)*b
}

// a % b == a - ((a // b) * b)
func FMod(a, b float64) float64 {
	if a > 0 && math.IsInf(b, 1) || a < 0 && math.IsInf(b, -1) {
		return a
	}
	if a > 0 && math.IsInf(b, -1) || a < 0 && math.IsInf(b, 1) {
		return b
	}
	return a - math.Floor(a/b)*b
}

func IFloorDiv(a, b int64) int64 {
	if a > 0 && b > 0 || a < 0 && b < 0 || a%b == 0 {
		return a / b
	} else {
		return a/b - 1
	}
}

func FFloorDiv(a, b float64) float64 {
	return math.Floor(a / b)
}

func ShiftLeft(a, n int64) int64 {
	if n >= 0 {
		return a << uint64(n)
	} else {
		return ShiftRight(a, -n)
	}
}

func ShiftRight(a, n int64) int64 {
	if n >= 0 {
		return int64(uint64(a) >> uint64(n))
	} else {
		return ShiftLeft(a, -n)
	}
}
