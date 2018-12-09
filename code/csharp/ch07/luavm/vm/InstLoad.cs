using LuaVm = luavm.api.LuaState;

namespace luavm.vm
{
    public class InstLoad
    {
        internal static void loadNil(Instruction i, LuaVm vm)
        {
            var ab_ = i.AsBx();
            var a = ab_.Item1 + 1;
            var b = ab_.Item2;

            vm.PushNil();
            for (var l = a; l <= a + b; l++)
            {
                vm.Copy(-1, l);
            }

            vm.Pop(1);
        }

        internal static void loadBool(Instruction i, LuaVm vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2;
            var c = abc.Item3;

            vm.PushBoolean(b != 0);
            vm.Replace(a);
            if (c != 0)
            {
                vm.AddPC(1);
            }
        }

        internal static void loadK(Instruction i, LuaVm vm)
        {
            var aBx = i.ABx();
            var a = aBx.Item1 + 1;
            var bx = aBx.Item2;

            vm.GetConst(bx);
            vm.Replace(a);
        }

        internal static void loadKx(Instruction i, LuaVm vm)
        {
            var aBx = i.ABx();
            var a = aBx.Item1 + 1;
            var ax = new Instruction(vm.Fetch()).Ax();

            vm.GetConst(ax);
            vm.Replace(a);
        }
    }
}