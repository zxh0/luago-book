use super::instruction::Instruction;
use crate::api::LuaVM;

// R(A), R(A+1), ..., R(A+B) := nil
pub fn load_nil(i: u32, vm: &mut LuaVM) {
    let (mut a, b, _) = i.abc();
    a += 1;

    vm.push_nil();
    for i in a..(a + b + 1) {
        vm.copy(-1, i);
    }
    vm.pop(1);
}

// R(A) := (bool)B; if (C) pc++
pub fn load_bool(i: u32, vm: &mut LuaVM) {
    let (mut a, b, c) = i.abc();
    a += 1;

    vm.push_boolean(b != 0);
    vm.replace(a);

    if c != 0 {
        vm.add_pc(1);
    }
}

// R(A) := Kst(Bx)
pub fn load_k(i: u32, vm: &mut LuaVM) {
    let (mut a, bx) = i.a_bx();
    a += 1;

    vm.get_const(bx);
    vm.replace(a);
}

// R(A) := Kst(extra arg)
pub fn load_kx(i: u32, vm: &mut LuaVM) {
    let (mut a, _) = i.a_bx();
    a += 1;
    let ax = vm.fetch().ax();

    //vm.CheckStack(1)
    vm.get_const(ax);
    vm.replace(a);
}
