using luavm.api;
using LuaVm = luavm.api.LuaState;
using ArithOp = System.Int32;
using CompareOp = System.Int32;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstFor
    {
        internal static void ForPrep(Instruction i, ref LuaVM vm)
        {
            var (a, sBx) = i.AsBx();
            a += 1;

            vm.PushValue(a);
            vm.PushValue(a + 2);
            vm.Arith(Constant.LUA_OPSUB);
            vm.Replace(a);
            vm.AddPC(sBx);
        }

        internal static void ForLoop(Instruction i, ref LuaVM vm)
        {
            var (a, sBx) = i.AsBx();
            a += 1;

            // R(A)+=R(A+2);
            vm.PushValue(a + 2);
            vm.PushValue(a);
            vm.Arith(Constant.LUA_OPADD);
            vm.Replace(a);

            var isPositiveStep = vm.ToNumber(a + 2) >= 0;
            if (
                (isPositiveStep && vm.Compare(a, a + 1, Constant.LUA_OPLE)) ||
                (!isPositiveStep && vm.Compare(a + 1, a, Constant.LUA_OPLE)))
            {
                // pc+=sBx; R(A+3)=R(A)
                vm.AddPC(sBx);
                vm.Copy(a, a + 3);
            }
        }

        // if R(A+1) ~= nil then {
        //   R(A)=R(A+1); pc += sBx
        // }
        internal static void TForLoop(Instruction i, ref LuaVM vm)
        {
            var (a, sBx ) = i.AsBx();
            a += 1;

            if (!vm.IsNil(a + 1))
            {
                vm.Copy(a + 1, a);
                vm.AddPC(sBx);
            }
        }
    }
}