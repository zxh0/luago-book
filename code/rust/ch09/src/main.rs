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
        ls.load(data, &filename, "b");
        ls.call(0, 0);
    }
    Ok(())
}
