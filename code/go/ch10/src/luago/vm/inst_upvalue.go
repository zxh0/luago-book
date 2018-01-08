package vm

import . "luago/api"

// R(A) := UpValue[B][RK(C)]
func getTabUp(i Instruction, vm LuaVM) {
	a, _, c := i.ABC()
	a += 1

	vm.PushGlobalTable()
	vm.GetRK(c)
	vm.GetTable(-2)
	vm.Replace(a)
	vm.Pop(1)
}
