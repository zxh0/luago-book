package vm

import . "luago/api"

// R(A), R(A+1), ..., R(A+B) := nil
func loadNil(i Instruction, vm LuaVM) {
	a, b, _ := i.ABC()
	a += 1

	vm.PushNil()
	for i := a; i <= a+b; i++ {
		vm.Copy(-1, i)
	}
	vm.Pop(1)
}

// R(A) := (bool)B; if (C) pc++
func loadBool(i Instruction, vm LuaVM) {
	a, b, c := i.ABC()
	a += 1

	vm.PushBoolean(b != 0)
	vm.Replace(a)

	if c != 0 {
		vm.AddPC(1)
	}
}

// R(A) := Kst(Bx)
func loadK(i Instruction, vm LuaVM) {
	a, bx := i.ABx()
	a += 1

	vm.GetConst(bx)
	vm.Replace(a)
}

// R(A) := Kst(extra arg)
func loadKx(i Instruction, vm LuaVM) {
	a, _ := i.ABx()
	a += 1
	ax := Instruction(vm.Fetch()).Ax()

	//vm.CheckStack(1)
	vm.GetConst(ax)
	vm.Replace(a)
}
