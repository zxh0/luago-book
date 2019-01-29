use crate::binary::chunk::Prototype;
use std::hash::{Hash, Hasher};
use std::rc::Rc;

pub struct Closure {
    pub proto: Rc<Prototype>,
    rdm: usize,
}

impl Hash for Closure {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.rdm.hash(state);
    }
}

impl Closure {
    pub fn new(proto: Rc<Prototype>) -> Closure {
        Closure {
            proto: proto,
            rdm: super::math::random(),
        }
    }
}
