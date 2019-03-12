using ArithOp = System.Int32;
using CompareOp = System.Int32;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public class InstUpvalue
    {
        internal static void GetUpval(Instruction i, ref LuaVM vm)
        {
            var (a, b, _) = i.ABC();
            a += 1;
            b += 1;

            vm.Copy(LuaVM.LuaUpvalueIndex(b), a);
        }


        // UpValue[B] := R(A)
        internal static void SetUpval(Instruction i, ref LuaVM vm)
        {
            var (a, b, _) = i.ABC();
            a += 1;
            b += 1;

            vm.Copy(a, LuaVM.LuaUpvalueIndex(b));
        }

        // R(A) := UpValue[B][RK(C)]
        internal static void GetTabUp(Instruction i, ref LuaVM vm)
        {
            var (a, b, c ) = i.ABC();
            a += 1;
            b += 1;

            vm.GetRK(c);
            vm.GetTable(LuaVM.LuaUpvalueIndex(b));
            vm.Replace(a);
        }

        // UpValue[A][RK(B)] := RK(C)
        internal static void SetTabUp(Instruction i, ref LuaVM vm)
        {
            var ( a, b, c) = i.ABC();
            a += 1;

            vm.GetRK(b);
            vm.GetRK(c);
            vm.SetTable(LuaVM.LuaUpvalueIndex(a));
        }
    }
}