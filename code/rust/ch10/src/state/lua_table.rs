use super::lua_value::LuaValue;
use std::cell::RefCell;
use std::collections::HashMap;
use std::hash::{Hash, Hasher};

// https://doc.rust-lang.org/std/cell/index.html#introducing-mutability-inside-of-something-immutable
#[derive(Clone)]
pub struct LuaTable {
    arr: RefCell<Vec<LuaValue>>,
    map: RefCell<HashMap<LuaValue, LuaValue>>,
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
            arr: RefCell::new(Vec::with_capacity(narr)),
            map: RefCell::new(HashMap::with_capacity(nrec)),
            rdm: super::math::random(),
        }
    }

    pub fn len(&self) -> usize {
        self.arr.borrow().len()
    }

    pub fn get(&self, key: &LuaValue) -> LuaValue {
        let arr = self.arr.borrow();
        if let Some(idx) = to_index(key) {
            if idx <= arr.len() {
                return arr[idx - 1].clone(); // TODO
            }
        }

        let map = self.map.borrow();
        if let Some(val) = map.get(key) {
            val.clone() // TODO
        } else {
            LuaValue::Nil
        }
    }

    pub fn put(&self, key: LuaValue, val: LuaValue) {
        if key.is_nil() {
            panic!("table index is nil!");
        }
        if let LuaValue::Number(n) = key {
            if n.is_nan() {
                panic!("table index is NaN!");
            }
        }

        let mut arr = self.arr.borrow_mut();
        let mut map = self.map.borrow_mut();
        if let Some(idx) = to_index(&key) {
            let arr_len = arr.len();
            if idx <= arr_len {
                let val_is_nil = val.is_nil();
                arr[idx - 1] = val;
                if idx == arr_len && val_is_nil {
                    shrink_array(&mut arr);
                }
                return;
            }
            if idx == arr_len + 1 {
                map.remove(&key);
                if !val.is_nil() {
                    arr.push(val);
                    expand_array(&mut arr, &mut map);
                }
                return;
            }
        }

        if !val.is_nil() {
            map.insert(key, val);
        } else {
            map.remove(&key);
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

fn shrink_array(arr: &mut Vec<LuaValue>) {
    while !arr.is_empty() {
        if arr.last().unwrap().is_nil() {
            arr.pop();
        } else {
            break;
        }
    }
}

fn expand_array(arr: &mut Vec<LuaValue>, map: &mut HashMap<LuaValue, LuaValue>) {
    let mut idx = arr.len() + 1;
    loop {
        let key = LuaValue::Integer(idx as i64);
        if map.contains_key(&key) {
            let val = map.remove(&key).unwrap();
            arr.push(val);
            idx += 1;
        } else {
            break;
        }
    }
}
