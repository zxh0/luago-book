use super::fpb::fb2int;
use super::instruction::Instruction;
use crate::api::LuaVM;

/* number of list items to accumulate before a SETLIST instruction */
const LFIELDS_PER_FLUSH: isize = 50;

// R(A) := {} (size = B,C)
pub fn new_table(i: u32, vm: &mut LuaVM) {
    let (mut a, b, c) = i.abc();
    a += 1;

    let narr = fb2int(b as usize);
    let nrec = fb2int(c as usize);
    vm.create_table(narr, nrec);
    vm.replace(a);
}

// R(A) := R(B)[RK(C)]
pub fn get_table(i: u32, vm: &mut LuaVM) {
    let (mut a, mut b, c) = i.abc();
    a += 1;
    b += 1;

    vm.get_rk(c);
    vm.get_table(b);
    vm.replace(a);
}

// R(A)[RK(B)] := RK(C)
pub fn set_table(i: u32, vm: &mut LuaVM) {
    let (mut a, b, c) = i.abc();
    a += 1;

    vm.get_rk(b);
    vm.get_rk(c);
    vm.set_table(a);
}

// R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
pub fn set_list(i: u32, vm: &mut LuaVM) {
    let (mut a, b, mut c) = i.abc();
    a += 1;

    if c > 0 {
        c = c - 1;
    } else {
        c = vm.fetch().ax();
    }

    vm.check_stack(1);
    let mut idx = (c * LFIELDS_PER_FLUSH) as i64;
    for j in 1..(b + 1) {
        idx += 1;
        vm.push_value(a + j);
        vm.set_i(a, idx);
    }
}
