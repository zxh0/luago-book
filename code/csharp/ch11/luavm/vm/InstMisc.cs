using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstMisc
    {
        internal static void move(Instruction i, ref LuaVM vm)
        {
            var ab_ = i.ABC();
            var a = ab_.Item1 + 1;
            var b = ab_.Item2 + 1;
            vm.Copy(b, a);
        }

        internal static void jmp(Instruction i, ref LuaVM vm)
        {
            var (a, sBx ) = i.AsBx();
            ;

            vm.AddPC(sBx);
            if (a != 0 )
            {
                vm.CloseUpvalues(a);
            }
        }
    }
}