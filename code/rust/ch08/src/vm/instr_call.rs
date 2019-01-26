use super::instruction::Instruction;
use crate::api::LuaVM;

// R(A+1) := R(B); R(A) := R(B)[RK(C)]
pub fn _self(i: u32, vm: &mut LuaVM) {
    let (mut a, mut b, c) = i.abc();
    a += 1;
    b += 1;

    vm.copy(b, a + 1);
    vm.get_rk(c);
    vm.get_table(b);
    vm.replace(a);
}

// R(A) := closure(KPROTO[Bx])
pub fn closure(i: u32, vm: &mut LuaVM) {
    let (mut a, bx) = i.a_bx();
    a += 1;

    vm.load_proto(bx as usize);
    vm.replace(a);
}

// R(A), R(A+1), ..., R(A+B-2) = vararg
pub fn vararg(i: u32, vm: &mut LuaVM) {
    let (mut a, b, _) = i.abc();
    a += 1;

    if b != 1 {
        // b==0 or b>1
        vm.load_vararg(b - 1);
        pop_results(a, b, vm)
    }
}

// return R(A)(R(A+1), ... ,R(A+B-1))
pub fn tail_call(i: u32, vm: &mut LuaVM) {
    let (mut a, b, _) = i.abc();
    a += 1;

    // todo: optimize tail call!
    let c = 0;
    let nargs = push_func_and_args(a, b, vm);
    vm.call(nargs, c - 1);
    pop_results(a, c, vm);
}

// R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
pub fn call(i: u32, vm: &mut LuaVM) {
    let (mut a, b, c) = i.abc();
    a += 1;

    // println(":::"+ vm.StackToString())
    let nargs = push_func_and_args(a, b, vm);
    vm.call(nargs, c - 1);
    pop_results(a, c, vm);
}

fn push_func_and_args(a: isize, b: isize, vm: &mut LuaVM) -> usize {
    if b >= 1 {
        vm.check_stack(b as usize);
        for i in a..(a + b) {
            vm.push_value(i);
        }
        b as usize - 1
    } else {
        fix_stack(a, vm);
        vm.get_top() as usize - vm.register_count() - 1
    }
}

fn fix_stack(a: isize, vm: &mut LuaVM) {
    let x = vm.to_integer(-1) as isize;
    vm.pop(1);

    vm.check_stack((x - a) as usize);
    for i in a..x {
        vm.push_value(i);
    }
    vm.rotate(vm.register_count() as isize + 1, x - a);
}

fn pop_results(a: isize, c: isize, vm: &mut LuaVM) {
    if c == 1 {
        // no results
    } else if c > 1 {
        for i in (a..(a + c - 1)).rev() {
            vm.replace(i);
        }
    } else {
        // leave results on stack
        vm.check_stack(1);
        vm.push_integer(a as i64);
    }
}

// return R(A), ... ,R(A+B-2)
pub fn _return(i: u32, vm: &mut LuaVM) {
    let (mut a, b, _) = i.abc();
    a += 1;

    if b == 1 {
        // no return values
    } else if b > 1 {
        // b-1 return values
        vm.check_stack(b as usize - 1);
        for i in a..(a + b - 1) {
            vm.push_value(i);
        }
    } else {
        fix_stack(a, vm);
    }
}
