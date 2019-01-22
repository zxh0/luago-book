mod arith_ops;
mod cmp_ops;
mod lua_stack;
mod lua_state;
mod lua_table;
mod lua_value;
mod math;

pub use self::lua_state::LuaState;
use crate::binary::chunk::Prototype;

pub fn new_lua_state(stack_size: usize, proto: Prototype) -> LuaState {
    LuaState::new(stack_size, proto)
}
