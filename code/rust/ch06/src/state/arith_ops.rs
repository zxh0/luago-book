use super::lua_value::LuaValue;

fn iadd(a: i64, b: i64) -> i64 {
    a + b
}
fn fadd(a: f64, b: f64) -> f64 {
    a + b
}
fn isub(a: i64, b: i64) -> i64 {
    a - b
}
fn fsub(a: f64, b: f64) -> f64 {
    a - b
}
fn imul(a: i64, b: i64) -> i64 {
    a * b
}
fn fmul(a: f64, b: f64) -> f64 {
    a * b
}
fn imod(a: i64, b: i64) -> i64 {
    super::math::i_mod(a, b)
}
fn fmod(a: f64, b: f64) -> f64 {
    super::math::f_mod(a, b)
}
fn pow(a: f64, b: f64) -> f64 {
    a.powf(b)
}
fn div(a: f64, b: f64) -> f64 {
    a / b
}
fn iidiv(a: i64, b: i64) -> i64 {
    super::math::i_floor_div(a, b)
}
fn fidiv(a: f64, b: f64) -> f64 {
    super::math::f_floor_div(a, b)
}
fn band(a: i64, b: i64) -> i64 {
    a & b
}
fn bor(a: i64, b: i64) -> i64 {
    a | b
}
fn bxor(a: i64, b: i64) -> i64 {
    a ^ b
}
fn shl(a: i64, b: i64) -> i64 {
    super::math::shift_left(a, b)
}
fn shr(a: i64, b: i64) -> i64 {
    super::math::shift_right(a, b)
}
fn iunm(a: i64, _: i64) -> i64 {
    -a
}
fn funm(a: f64, _: f64) -> f64 {
    -a
}
fn bnot(a: i64, _: i64) -> i64 {
    !a
}

fn inone(_: i64, _: i64) -> i64 {
    0
}
fn fnone(_: f64, _: f64) -> f64 {
    0.0
}

pub const OPS: &'static [(fn(i64, i64) -> i64, fn(f64, f64) -> f64)] = &[
    (iadd, fadd),
    (isub, fsub),
    (imul, fmul),
    (imod, fmod),
    (inone, pow),
    (inone, div),
    (iidiv, fidiv),
    (band, fnone),
    (bor, fnone),
    (bxor, fnone),
    (shl, fnone),
    (shr, fnone),
    (iunm, funm),
    (bnot, fnone),
];

pub fn arith(a: &LuaValue, b: &LuaValue, op: u8) -> Option<LuaValue> {
    let iop = OPS[op as usize].0;
    let fop = OPS[op as usize].1;
    if fop == fnone {
        // bitwise
        if let Some(x) = a.to_integer() {
            if let Some(y) = b.to_integer() {
                return Some(LuaValue::Integer(iop(x, y)));
            }
        }
    } else {
        // arith
        if iop != inone {
            // add,sub,mul,mod,idiv,unm
            if let LuaValue::Integer(x) = a {
                if let LuaValue::Integer(y) = b {
                    return Some(LuaValue::Integer(iop(*x, *y)));
                }
            }
        }
        if let Some(x) = a.to_number() {
            if let Some(y) = b.to_number() {
                return Some(LuaValue::Number(fop(x, y)));
            }
        }
    }
    None
}
