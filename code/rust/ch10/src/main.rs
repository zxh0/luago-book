mod api;
mod binary;
mod state;
mod vm;

use crate::api::LuaAPI;
use std::env;
use std::fs::File;
use std::io;
use std::io::prelude::*;

fn main() -> io::Result<()> {
    if env::args().count() > 1 {
        let filename = env::args().nth(1).unwrap();
        let mut file = File::open(&filename)?;

        let mut data = Vec::new();
        file.read_to_end(&mut data)?;

        let mut ls = state::new_lua_state();
        ls.register("print", print);
        ls.load(data, &filename, "b");
        ls.call(0, 0);
    }
    Ok(())
}

fn print(ls: &LuaAPI) -> usize {
    let nargs = ls.get_top();
    for i in 1..(nargs + 1) {
        if ls.is_boolean(i) {
            print!("{}", ls.to_boolean(i));
        } else if ls.is_string(i) {
            print!("{}", ls.to_string(i));
        } else {
            print!("{}", ls.type_name(ls.type_id(i)));
        }
        if i < nargs {
            print!("\t")
        }
    }
    println!("");
    return 0;
}
