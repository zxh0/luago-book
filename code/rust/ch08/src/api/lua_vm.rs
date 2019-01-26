pub trait LuaVM: super::lua_state::LuaState {
    fn pc(&self) -> isize;
    fn add_pc(&mut self, n: isize);
    fn fetch(&mut self) -> u32;
    fn get_const(&mut self, idx: isize);
    fn get_rk(&mut self, rk: isize);
    fn register_count(&self) -> usize;
    fn load_vararg(&mut self, n: isize);
    fn load_proto(&mut self, idx: usize);
}
