use super::lua_value::LuaValue;
use std::collections::HashMap;
use std::hash::{Hash, Hasher};

#[derive(Clone)]
pub struct LuaTable {
    arr: Vec<LuaValue>,
    map: HashMap<LuaValue, LuaValue>,
    rdm: usize, // hash code
}

impl Hash for LuaTable {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.rdm.hash(state);
    }
}

impl LuaTable {
    pub fn new(narr: usize, nrec: usize) -> LuaTable {
        LuaTable {
            arr: Vec::with_capacity(narr),
            map: HashMap::with_capacity(nrec),
            rdm: super::math::random(),
        }
    }

    pub fn len(&self) -> usize {
        self.arr.len()
    }

    pub fn get(&self, key: &LuaValue) -> LuaValue {
        if let Some(idx) = to_index(key) {
            if idx <= self.arr.len() {
                return self.arr[idx - 1].clone(); // TODO
            }
        }
        if let Some(val) = self.map.get(key) {
            val.clone() // TODO
        } else {
            LuaValue::Nil
        }
    }

    pub fn put(&mut self, key: LuaValue, val: LuaValue) {
        if key.is_nil() {
            panic!("table index is nil!");
        }
        if let LuaValue::Number(n) = key {
            if n.is_nan() {
                panic!("table index is NaN!");
            }
        }

        if let Some(idx) = to_index(&key) {
            let arr_len = self.arr.len();
            if idx <= arr_len {
                let val_is_nil = val.is_nil();
                self.arr[idx - 1] = val;
                if idx == arr_len && val_is_nil {
                    self.shrink_array();
                }
                return;
            }
            if idx == arr_len + 1 {
                self.map.remove(&key);
                if !val.is_nil() {
                    self.arr.push(val);
                    self.expand_array();
                }
                return;
            }
        }

        if !val.is_nil() {
            self.map.insert(key, val);
        } else {
            self.map.remove(&key);
        }
    }

    fn shrink_array(&mut self) {
        while !self.arr.is_empty() {
            if self.arr.last().unwrap().is_nil() {
                self.arr.pop();
            } else {
                break;
            }
        }
    }

    fn expand_array(&mut self) {
        let mut idx = self.arr.len() + 1;
        loop {
            let key = LuaValue::Integer(idx as i64);
            if self.map.contains_key(&key) {
                let val = self.map.remove(&key).unwrap();
                self.arr.push(val);
                idx += 1;
            } else {
                break;
            }
        }
    }
}

fn to_index(key: &LuaValue) -> Option<usize> {
    if let LuaValue::Integer(i) = key {
        if *i >= 1 {
            return Some(*i as usize);
        }
    } else if let LuaValue::Number(n) = key {
        if let Some(i) = super::math::float_to_integer(*n) {
            if i >= 1 {
                return Some(i as usize);
            }
        }
    }
    None
}
