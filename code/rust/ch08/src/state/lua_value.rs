use super::lua_table::LuaTable;
use super::math::float_to_integer;
use crate::api::consts::*;
use std::cell::RefCell;
use std::hash::{Hash, Hasher};
use std::rc::Rc;

#[derive(Clone, PartialEq)]
pub enum LuaValue {
    Nil,
    Boolean(bool),
    Integer(i64),
    Number(f64),
    Str(String),                  // TODO
    Table(Rc<RefCell<LuaTable>>), // https://doc.rust-lang.org/std/cell/index.html#introducing-mutability-inside-of-something-immutable
}

// the trait `std::cmp::Eq` is not implemented for `f64`
impl Eq for LuaValue {} // TODO

// the trait `std::hash::Hash` is not implemented for `f64`
impl Hash for LuaValue {
    fn hash<H: Hasher>(&self, state: &mut H) {
        match self {
            LuaValue::Nil => 0.hash(state),
            LuaValue::Boolean(b) => b.hash(state),
            LuaValue::Integer(i) => i.hash(state),
            LuaValue::Number(n) => n.to_bits().hash(state),
            LuaValue::Str(s) => s.hash(state),
            LuaValue::Table(t) => t.borrow().hash(state),
        }
    }
}

impl LuaValue {
    pub fn new_table(narr: usize, nrec: usize) -> LuaValue {
        LuaValue::Table(Rc::new(RefCell::new(LuaTable::new(narr, nrec))))
    }

    pub fn is_nil(&self) -> bool {
        match self {
            LuaValue::Nil => true,
            _ => false,
        }
    }

    pub fn type_id(&self) -> i8 {
        match self {
            LuaValue::Nil => LUA_TNIL,
            LuaValue::Boolean(_) => LUA_TBOOLEAN,
            LuaValue::Number(_) => LUA_TNUMBER,
            LuaValue::Integer(_) => LUA_TNUMBER,
            LuaValue::Str(_) => LUA_TSTRING,
            LuaValue::Table(_) => LUA_TTABLE,
        }
    }

    pub fn to_boolean(&self) -> bool {
        match self {
            LuaValue::Nil => false,
            LuaValue::Boolean(b) => *b, // TODO
            _ => true,
        }
    }

    // http://www.lua.org/manual/5.3/manual.html#3.4.3
    pub fn to_number(&self) -> Option<f64> {
        match self {
            LuaValue::Integer(i) => Some(*i as f64),
            LuaValue::Number(n) => Some(*n),
            LuaValue::Str(s) => s.parse::<f64>().ok(), // TODO
            _ => None,
        }
    }

    // http://www.lua.org/manual/5.3/manual.html#3.4.3
    pub fn to_integer(&self) -> Option<i64> {
        match self {
            LuaValue::Integer(i) => Some(*i),
            LuaValue::Number(n) => float_to_integer(*n),
            LuaValue::Str(s) => string_to_integer(s),
            _ => None,
        }
    }
}

fn string_to_integer(s: &String) -> Option<i64> {
    if let Ok(i) = s.parse::<i64>() {
        Some(i)
    } else if let Ok(n) = s.parse::<f64>() {
        float_to_integer(n)
    } else {
        None
    }
}
