use super::instruction::Instruction;
use crate::api::LuaVM;

// R(A) := UpValue[B][RK(C)]
pub fn get_tab_up(i: u32, vm: &mut LuaVM) {
    let (mut a, _, c) = i.abc();
    a += 1;

    vm.push_global_table();
    vm.get_rk(c);
    vm.get_table(-2);
    vm.replace(a);
    vm.pop(1);
}
