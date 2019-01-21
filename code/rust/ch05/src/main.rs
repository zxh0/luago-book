mod api;
mod state;
use crate::api::{consts::*, LuaAPI};
use crate::state::LuaState;

fn main() {
    let mut ls = state::new_lua_state();

    ls.push_integer(1);
    ls.push_string("2.0".to_string());
    ls.push_string("3.0".to_string());
    ls.push_number(4.0);
    print_stack(&ls);

    ls.arith(LUA_OPADD);
    print_stack(&ls);
    ls.arith(LUA_OPBNOT);
    print_stack(&ls);
    ls.len(2);
    print_stack(&ls);
    ls.concat(3);
    print_stack(&ls);
    let x = ls.compare(1, 2, LUA_OPEQ);
    ls.push_boolean(x);
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
