use crate::api::RustFn;
use crate::binary::chunk::Prototype;
use std::hash::{Hash, Hasher};
use std::rc::Rc;

pub enum Upvalue {
    Nil,
    Globals, // TODO
}

pub struct Closure {
    pub proto: Rc<Prototype>,
    pub rust_fn: Option<RustFn>,
    pub upvals: Vec<Upvalue>,
    rdm: usize,
}

impl Hash for Closure {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.rdm.hash(state);
    }
}

impl Closure {
    pub fn new_fake_closure() -> Closure {
        Closure {
            proto: new_empty_prototype(), // TODO
            rust_fn: None,
            upvals: vec![],
            rdm: super::math::random(),
        }
    }

    pub fn new_lua_closure(proto: Rc<Prototype>) -> Closure {
        let uvs = new_upvalue_vec(proto.upvalues.len());
        Closure {
            proto: proto,
            rust_fn: None,
            upvals: uvs,
            rdm: super::math::random(),
        }
    }

    pub fn new_rust_closure(f: RustFn, nuvs: usize) -> Closure {
        Closure {
            proto: new_empty_prototype(), // TODO
            rust_fn: Some(f),
            upvals: new_upvalue_vec(nuvs),
            rdm: super::math::random(),
        }
    }
}

fn new_upvalue_vec(n: usize) -> Vec<Upvalue> {
    let mut vec = Vec::with_capacity(n);
    for _ in 0..n {
        vec.push(Upvalue::Nil);
    }
    vec
}

fn new_empty_prototype() -> Rc<Prototype> {
    Rc::new(Prototype {
        source: None, // debug
        line_defined: 0,
        last_line_defined: 0,
        num_params: 0,
        is_vararg: 0,
        max_stack_size: 0,
        code: vec![],
        constants: vec![],
        upvalues: vec![],
        protos: vec![],
        line_info: vec![],     // debug
        loc_vars: vec![],      // debug
        upvalue_names: vec![], // debug
    })
}
