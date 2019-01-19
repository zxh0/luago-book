use super::lua_stack::LuaStack;
use super::lua_value::LuaValue;
use crate::api::consts::*;
use crate::api::LuaAPI;

pub struct LuaState {
    stack: LuaStack,
}

impl LuaState {
    pub fn new() -> LuaState {
        LuaState {
            stack: LuaStack::new(20),
        }
    }
}

impl LuaAPI for LuaState {
    /* basic stack manipulation */

    fn get_top(&self) -> isize {
        self.stack.top()
    }

    fn abs_index(&self, idx: isize) -> isize {
        self.stack.abs_index(idx)
    }

    fn check_stack(&mut self, n: usize) -> bool {
        self.stack.check(n);
        true
    }

    fn pop(&mut self, n: usize) {
        for _ in 0..n {
            self.stack.pop();
        }
    }

    fn copy(&mut self, from_idx: isize, to_idx: isize) {
        let val = self.stack.get(from_idx);
        self.stack.set(to_idx, val);
    }

    fn push_value(&mut self, idx: isize) {
        let val = self.stack.get(idx);
        self.stack.push(val);
    }

    fn replace(&mut self, idx: isize) {
        let val = self.stack.pop();
        self.stack.set(idx, val);
    }

    fn insert(&mut self, idx: isize) {
        self.rotate(idx, 1);
    }

    fn remove(&mut self, idx: isize) {
        self.rotate(idx, -1);
        self.pop(1);
    }

    fn rotate(&mut self, idx: isize, n: isize) {
        let abs_idx = self.stack.abs_index(idx);
        if abs_idx < 0 || !self.stack.is_valid(abs_idx) {
            panic!("invalid index!");
        }

        let t = self.stack.top() - 1; /* end of stack segment being rotated */
        let p = abs_idx - 1; /* start of segment */
        let m = if n >= 0 { t - n } else { p - n - 1 }; /* end of prefix */
        self.stack.reverse(p as usize, m as usize); /* reverse the prefix with length 'n' */
        self.stack.reverse(m as usize + 1, t as usize); /* reverse the suffix */
        self.stack.reverse(p as usize, t as usize); /* reverse the entire segment */
    }

    fn set_top(&mut self, idx: isize) {
        let new_top = self.stack.abs_index(idx);
        if new_top < 0 {
            panic!("stack underflow!");
        }

        let n = self.stack.top() - new_top;
        if n > 0 {
            for _ in 0..n {
                self.stack.pop();
            }
        } else if n < 0 {
            for _ in n..0 {
                self.stack.push(LuaValue::Nil);
            }
        }
    }

    /* access functions (stack -> rust) */

    fn type_name(&self, tp: i8) -> &str {
        match tp {
            LUA_TNONE => "no value",
            LUA_TNIL => "nil",
            LUA_TBOOLEAN => "boolean",
            LUA_TNUMBER => "number",
            LUA_TSTRING => "string",
            LUA_TTABLE => "table",
            LUA_TFUNCTION => "function",
            LUA_TTHREAD => "thread",
            LUA_TLIGHTUSERDATA => "userdata",
            LUA_TUSERDATA => "userdata",
            _ => "?", // TODO
        }
    }

    fn type_id(&self, idx: isize) -> i8 {
        if self.stack.is_valid(idx) {
            self.stack.get(idx).type_id()
        } else {
            LUA_TNONE
        }
    }

    fn is_none(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TNONE
    }

    fn is_nil(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TNIL
    }

    fn is_none_or_nil(&self, idx: isize) -> bool {
        self.type_id(idx) <= LUA_TNIL
    }

    fn is_boolean(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TBOOLEAN
    }

    fn is_table(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TTABLE
    }

    fn is_function(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TFUNCTION
    }

    fn is_thread(&self, idx: isize) -> bool {
        self.type_id(idx) == LUA_TTHREAD
    }

    fn is_string(&self, idx: isize) -> bool {
        let t = self.type_id(idx);
        t == LUA_TSTRING || t == LUA_TNUMBER
    }

    fn is_number(&self, idx: isize) -> bool {
        self.to_numberx(idx).is_some()
    }

    fn is_integer(&self, idx: isize) -> bool {
        match self.stack.get(idx) {
            LuaValue::Integer(_) => true,
            _ => false,
        }
    }

    fn to_boolean(&self, idx: isize) -> bool {
        self.stack.get(idx).to_boolean()
    }

    fn to_integer(&self, idx: isize) -> i64 {
        self.to_integerx(idx).unwrap()
    }

    fn to_integerx(&self, idx: isize) -> Option<i64> {
        match self.stack.get(idx) {
            LuaValue::Integer(i) => Some(i),
            _ => None,
        }
    }

    fn to_number(&self, idx: isize) -> f64 {
        self.to_numberx(idx).unwrap()
    }

    fn to_numberx(&self, idx: isize) -> Option<f64> {
        match self.stack.get(idx) {
            LuaValue::Number(n) => Some(n),
            LuaValue::Integer(i) => Some(i as f64),
            _ => None,
        }
    }

    fn to_string(&self, idx: isize) -> String {
        self.to_stringx(idx).unwrap()
    }

    fn to_stringx(&self, idx: isize) -> Option<String> {
        match self.stack.get(idx) {
            LuaValue::Str(s) => Some(s),
            LuaValue::Number(n) => Some(n.to_string()),
            LuaValue::Integer(i) => Some(i.to_string()),
            _ => None,
        }
    }

    /* push functions (rust -> stack) */

    fn push_nil(&mut self) {
        self.stack.push(LuaValue::Nil);
    }

    fn push_boolean(&mut self, b: bool) {
        self.stack.push(LuaValue::Boolean(b));
    }

    fn push_integer(&mut self, n: i64) {
        self.stack.push(LuaValue::Integer(n));
    }

    fn push_number(&mut self, n: f64) {
        self.stack.push(LuaValue::Number(n));
    }

    fn push_string(&mut self, s: String) {
        self.stack.push(LuaValue::Str(s));
    }

    /* comparison and arithmetic functions */

    fn arith(&mut self, op: u8) {
        if op != LUA_OPUNM && op != LUA_OPBNOT {
            let b = self.stack.pop();
            let a = self.stack.pop();
            if let Some(result) = super::arith_ops::arith(&a, &b, op) {
                self.stack.push(result);
                return;
            }
        } else {
            let a = self.stack.pop();
            if let Some(result) = super::arith_ops::arith(&a, &a, op) {
                self.stack.push(result);
                return;
            }
        }
        panic!("arithmetic error!");
    }

    fn compare(&self, idx1: isize, idx2: isize, op: u8) -> bool {
        if !self.stack.is_valid(idx1) || !self.stack.is_valid(idx2) {
            false
        } else {
            let a = self.stack.get(idx1);
            let b = self.stack.get(idx2);
            if let Some(result) = super::cmp_ops::compare(&a, &b, op) {
                return result;
            }
            panic!("comparison error!")
        }
    }

    /* miscellaneous functions */

    fn len(&mut self, idx: isize) {
        let val = self.stack.get(idx);
        if let LuaValue::Str(s) = val {
            self.stack.push(LuaValue::Integer(s.len() as i64));
        } else {
            panic!("length error!")
        }
    }

    fn concat(&mut self, n: isize) {
        if n == 0 {
            self.stack.push(LuaValue::Str(String::new()))
        } else if n >= 2 {
            for _ in 1..n {
                if self.is_string(-1) && self.is_string(-2) {
                    let s2 = self.to_string(-1);
                    let mut s1 = self.to_string(-2);
                    s1.push_str(&s2);
                    self.stack.pop();
                    self.stack.pop();
                    self.stack.push(LuaValue::Str(s1));
                } else {
                    panic!("concatenation error!");
                }
            }
        }
        // n == 1, do nothing
    }
}
