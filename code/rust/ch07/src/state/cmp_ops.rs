use super::lua_value::LuaValue;
use crate::api::consts::*;

pub fn compare(a: &LuaValue, b: &LuaValue, op: u8) -> Option<bool> {
    match op {
        LUA_OPEQ => Some(eq(a, b)),
        LUA_OPLT => lt(a, b),
        LUA_OPLE => le(a, b),
        _ => None,
    }
}

macro_rules! cmp {
    ($a:ident $op:tt $b:ident) => {
        match $a {
            LuaValue::Str(x) => match $b {
                LuaValue::Str(y) => Some(x $op y),
                _ => None,
            },
            LuaValue::Integer(x) => match $b {
                LuaValue::Integer(y) => Some(x $op y),
                LuaValue::Number(y) => Some((*x as f64) $op *y),
                _ => None,
            },
            LuaValue::Number(x) => match $b {
                LuaValue::Number(y) => Some(x $op y),
                LuaValue::Integer(y) => Some(*x $op (*y as f64)),
                _ => None,
            },
            _ => None,
        }
    }
}

fn eq(a: &LuaValue, b: &LuaValue) -> bool {
    if let Some(x) = cmp!(a == b) {
        x
    } else {
        match a {
            LuaValue::Nil => match b {
                LuaValue::Nil => true,
                _ => false,
            },
            LuaValue::Boolean(x) => match b {
                LuaValue::Boolean(y) => x == y,
                _ => false,
            },
            _ => false,
        }
    }
}

fn lt(a: &LuaValue, b: &LuaValue) -> Option<bool> {
    cmp!(a < b)
}

fn le(a: &LuaValue, b: &LuaValue) -> Option<bool> {
    cmp!(a <= b)
}
