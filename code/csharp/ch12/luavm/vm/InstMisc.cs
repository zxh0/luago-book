using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstMisc
    {
        internal static void Move(Instruction i, ref LuaVM vm)
        {
            var (a, b, _) = i.ABC();
            a += 1;
            b += 1;
            vm.Copy(b, a);
        }

        internal static void Jmp(Instruction i, ref LuaVM vm)
        {
            var (a, sBx ) = i.AsBx();

            vm.AddPC(sBx);
            if (a != 0)
            {
                vm.CloseUpvalues(a);
            }
        }
    }
}