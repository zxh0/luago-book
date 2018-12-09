using LuaVm = luavm.api.LuaState;
using ArithOp = System.Int32;
using CompareOp = System.Int32;

namespace luavm.vm
{
    public static class InstTable
    {
        internal static void newTable(Instruction i, LuaVm vm)
        {
            var abc = i.ABC();
            var a = abc.Item1;
            var b = abc.Item2;
            var c = abc.Item3;
            a += 1;
            vm.CreateTable(Fpb.Fb2int(b), Fpb.Fb2int(c));
            vm.Replace(a);
        }

        internal static void getTable(Instruction i, LuaVm vm)
        {
            var abc = i.ABC();
            var a = abc.Item1;
            var b = abc.Item2;
            var c = abc.Item3;

            a += 1;
            b += 1;
            vm.GetRK(c);
            vm.GetTable(b);
            vm.Replace(a);
        }

        internal static void setTable(Instruction i, LuaVm vm)
        {
            var abc = i.ABC();
            var a = abc.Item1;
            var b = abc.Item2;
            var c = abc.Item3;

            a += 1;

            vm.GetRK(b);
            vm.GetRK(c);
            vm.SetTable(a);
        }

        internal static void setList(Instruction i, LuaVm vm)
        {
            var abc = i.ABC();
            var a = abc.Item1;
            var b = abc.Item2;
            var c = abc.Item3;

            a += 1;
            if (c > 0)
            {
                c = c - 1;
            }
            else
            {
                c = new Instruction(vm.Fetch()).Ax();
            }

            var idx = (long) c * LFIELDS_PER_FLUSH;
            for (var j = 1; j <= b; j++)
            {
                idx++;
                vm.PushValue(a + j);
                vm.SetI(a, idx);
            }
        }

        private const int LFIELDS_PER_FLUSH = 50;
    }
}