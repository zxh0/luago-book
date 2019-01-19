pub trait LuaVM: super::lua_state::LuaState {
    fn pc() -> isize;
    fn add_pc(n: isize);
    fn fetch() -> u32;
    fn get_const(idx: isize);
    fn get_rk(rk: isize);
}
