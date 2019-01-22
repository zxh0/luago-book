/*
** converts an integer to a "floating point byte", represented as
** (eeeeexxx), where the real value is (1xxx) * 2^(eeeee - 1) if
** eeeee != 0 and (xxx) otherwise.
 */
#[allow(dead_code)]
pub fn int2fb(mut x: usize) -> usize {
    let mut e = 0; /* exponent */
    if x < 8 {
        return x;
    }
    while x >= (8 << 4) {
        /* coarse steps */
        x = (x + 0xf) >> 4; /* x = ceil(x / 16) */
        e += 4;
    }
    while x >= (8 << 1) {
        /* fine steps */
        x = (x + 1) >> 1; /* x = ceil(x / 2) */
        e += 1;
    }
    return ((e + 1) << 3) | (x - 8);
}

/* converts back */
pub fn fb2int(x: usize) -> usize {
    if x < 8 {
        x
    } else {
        ((x & 7) + 8) << ((x >> 3) - 1)
    }
}
