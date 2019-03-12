using ArithOp = System.Int32;
using CompareOp = System.Int32;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstTable
    {
        internal static void newTable(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;

            vm.CreateTable(Fpb.Fb2int(b), Fpb.Fb2int(c));
            vm.Replace(a);
        }

        internal static void getTable(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;
            b += 1;

            vm.GetRK(c);
            vm.GetTable(b);
            vm.Replace(a);
        }

        internal static void setTable(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;

            vm.GetRK(b);
            vm.GetRK(c);
            vm.SetTable(a);
        }

        internal static void setList(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;

            if (c > 0)
            {
                c = c - 1;
            }
            else
            {
                c = new Instruction(vm.Fetch()).Ax();
            }

            var bIsZero = b == 0;
            if (bIsZero)
            {
                b = (int) vm.ToInteger(-1) - a - 1;
                vm.Pop(1);
            }

            vm.CheckStack(1);
            var idx = (long) (c * LFIELDS_PER_FLUSH);
            for (var j = 1; j <= b; j++)
            {
                idx++;
                vm.PushValue(a + j);
                vm.SetI(a, idx);
            }

            if (bIsZero)
            {
                for (var j = vm.RegisterCount() + 1; j <= vm.GetTop(); j++)
                {
                    idx++;
                    vm.PushValue(j);
                    vm.SetI(a, idx);
                }

                // clear stack
                vm.SetTop(vm.RegisterCount());
            }
        }

        private const int LFIELDS_PER_FLUSH = 50;
    }
}