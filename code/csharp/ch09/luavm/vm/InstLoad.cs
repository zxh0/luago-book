using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstLoad
    {
        internal static void loadNil(Instruction i, ref LuaVM vm)
        {
            var (a, b) = i.AsBx();
            a += 1;

            vm.PushNil();
            for (var l = a; l <= a + b; l++)
            {
                vm.Copy(-1, l);
            }

            vm.Pop(1);
        }

        internal static void loadBool(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;

            vm.PushBoolean(b != 0);
            vm.Replace(a);
            if (c != 0)
            {
                vm.AddPC(1);
            }
        }

        internal static void loadK(Instruction i, ref LuaVM vm)
        {
            var (a, bx) = i.ABx();
            a += 1;

            vm.GetConst(bx);
            vm.Replace(a);
        }

        internal static void loadKx(Instruction i, ref LuaVM vm)
        {
            var (a, _) = i.ABx();
            a += 1;
            var ax = new Instruction(vm.Fetch()).Ax();

            vm.GetConst(ax);
            vm.Replace(a);
        }
    }
}