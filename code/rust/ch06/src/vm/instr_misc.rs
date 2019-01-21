use super::instruction::Instruction;
use crate::api::LuaVM;

// R(A) := R(B)
pub fn _move(i: u32, vm: &mut LuaVM) {
    let (mut a, mut b, _) = i.abc();
    a += 1;
    b += 1;

    vm.copy(b, a);
}

// pc+=sBx; if (A) close all upvalues >= R(A - 1)
pub fn jmp(i: u32, vm: &mut LuaVM) {
    let (a, sbx) = i.a_sbx();

    vm.add_pc(sbx);
    if a != 0 {
        panic!("todo: jmp!");
    }
}
