mod api;
mod state;
use crate::api::{consts::*, LuaAPI};
use crate::state::LuaState;

fn main() {
    let mut ls = state::new_lua_state();

    ls.push_boolean(true);
    print_stack(&ls);
    ls.push_integer(10);
    print_stack(&ls);
    ls.push_nil();
    print_stack(&ls);
    ls.push_string("hello".to_string());
    print_stack(&ls);
    ls.push_value(-4);
    print_stack(&ls);
    ls.replace(3);
    print_stack(&ls);
    ls.set_top(6);
    print_stack(&ls);
    ls.remove(-3);
    print_stack(&ls);
    ls.set_top(-5);
    print_stack(&ls);
}

fn print_stack(ls: &LuaState) {
    let top = ls.get_top();
    for i in 1..top + 1 {
        let t = ls.type_id(i);
        match t {
            LUA_TBOOLEAN => print!("[{}]", ls.to_boolean(i)),
            LUA_TNUMBER => print!("[{}]", ls.to_number(i)),
            LUA_TSTRING => print!("[{:?}]", ls.to_string(i)),
            _ => print!("[{}]", ls.type_name(t)), // other values
        }
    }
    println!("");
}
