use super::closure::Closure;
use super::lua_value::LuaValue;
use crate::api::consts::LUA_REGISTRYINDEX;
use std::rc::Rc;

pub struct LuaStack {
    vec: Vec<LuaValue>, // slots
    registry: LuaValue,
    pub closure: Rc<Closure>, // TODO
    pub varargs: Vec<LuaValue>,
    pub pc: isize,
}

impl LuaStack {
    pub fn new(size: usize, registry: LuaValue, closure: Rc<Closure>) -> LuaStack {
        LuaStack {
            vec: Vec::with_capacity(size),
            registry: registry,
            closure: closure,
            varargs: Vec::new(),
            pc: 0,
        }
    }

    pub fn top(&self) -> isize {
        self.vec.len() as isize
    }

    pub fn check(&mut self, n: usize) {
        self.vec.reserve(n);
    }

    pub fn push(&mut self, val: LuaValue) {
        self.vec.push(val);
    }

    pub fn pop(&mut self) -> LuaValue {
        self.vec.pop().unwrap()
    }

    pub fn pop_n(&mut self, n: usize) -> Vec<LuaValue> {
        let mut vec = Vec::with_capacity(n);
        for _ in 0..n {
            vec.push(self.pop());
        }
        vec.reverse();
        vec
    }

    pub fn push_n(&mut self, mut vals: Vec<LuaValue>, n: isize) {
        vals.reverse();
        let nvals = vals.len();
        let un = if n < 0 { nvals } else { n as usize };

        for i in 0..un {
            if i < nvals {
                self.push(vals.pop().unwrap());
            } else {
                self.push(LuaValue::Nil);
            }
        }
    }

    pub fn set_top(&mut self, idx: isize) {
        let new_top = self.abs_index(idx);
        if new_top < 0 {
            panic!("stack underflow!");
        }

        let n = self.top() - new_top;
        if n > 0 {
            for _ in 0..n {
                self.pop();
            }
        } else if n < 0 {
            for _ in n..0 {
                self.push(LuaValue::Nil);
            }
        }
    }

    pub fn abs_index(&self, idx: isize) -> isize {
        if idx >= 0 || idx <= LUA_REGISTRYINDEX {
            idx
        } else {
            idx + self.top() + 1
        }
    }

    pub fn is_valid(&self, idx: isize) -> bool {
        if idx == LUA_REGISTRYINDEX {
            return true;
        }
        let abs_idx = self.abs_index(idx);
        abs_idx > 0 && abs_idx <= self.top()
    }

    pub fn get(&self, idx: isize) -> LuaValue {
        if idx == LUA_REGISTRYINDEX {
            return self.registry.clone();
        }
        let abs_idx = self.abs_index(idx);
        if abs_idx > 0 && abs_idx <= self.top() {
            let idx = abs_idx as usize - 1;
            self.vec[idx].clone() // TODO
        } else {
            LuaValue::Nil
        }
    }

    #[allow(dead_code)]
    pub fn peek(&self, idx: isize) -> &LuaValue {
        let abs_idx = self.abs_index(idx);
        if abs_idx > 0 && abs_idx <= self.top() {
            let idx = abs_idx as usize - 1;
            &self.vec[idx]
        } else {
            &LuaValue::Nil
        }
    }

    pub fn set(&mut self, idx: isize, val: LuaValue) {
        if idx == LUA_REGISTRYINDEX {
            self.registry = val;
            return;
        }
        let abs_idx = self.abs_index(idx);
        if abs_idx > 0 && abs_idx <= self.top() {
            let idx = abs_idx as usize - 1;
            self.vec[idx] = val;
        } else {
            panic!("invalid index: {}", idx);
        }
    }

    pub fn reverse(&mut self, mut from: usize, mut to: usize) {
        while from < to {
            self.vec.swap(from, to);
            from += 1;
            to -= 1;
        }
    }
}
