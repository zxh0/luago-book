using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstCall
    {
        // R(A) := closure(KPROTO[Bx])
        internal static void Closure(Instruction i, ref LuaVM vm)
        {
            var (a, Bx) = i.ABx();
            a += 1;

            vm.LoadProto(Bx);
            vm.Replace(a);
        }

        // R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
        internal static void Call(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;
            // println(":::"+ vm.StackToString())
            var nArgs = PushFuncAndArgs(a, b, ref vm);
            vm.Call(nArgs, c - 1);
            PopResults(a, c, ref vm);
        }

        static int PushFuncAndArgs(int a, int b, ref LuaVM vm)
        {
            if (b >= 1)
            {
                vm.CheckStack(b);
                for (var i = a; i < a + b; i++)
                {
                    vm.PushValue(i);
                }

                return b - 1;
            }

            FixStack(a, vm);
            return vm.GetTop() - vm.RegisterCount() - 1;
        }

        private static void FixStack(int a, LuaVM vm)
        {
            var x = (int) vm.ToInteger(-1);
            vm.Pop(1);

            vm.CheckStack(x - a);
            for (var i = a; i < x; i++)
            {
                vm.PushValue(i);
            }

            vm.Rotate(vm.RegisterCount() + 1, x - a);
        }

        private static void PopResults(int a, int c, ref LuaVM vm)
        {
            if (c == 1)
            {
                // no results
            }
            else if (c > 1)
            {
                for (var i = a + c - 2; i >= a; i--)
                {
                    vm.Replace(i);
                }
            }
            else
            {
                // leave results on stack
                vm.CheckStack(1);
                vm.PushInteger(a);
            }
        }

        // return R(A), ... ,R(A+B-2)
        internal static void _return(Instruction i, ref LuaVM vm)
        {
            var (a, b, _) = i.ABC();
            a += 1;

            if (b == 1)
            {
                // no return values
            }
            else if (b > 1)
            {
                // b-1 return values
                vm.CheckStack(b - 1);
                for (var j = a; j <= a + b - 2; j++)
                {
                    vm.PushValue(j);
                }
            }
            else
            {
                FixStack(a, vm);
            }
        }

        // R(A), R(A+1), ..., R(A+B-2) = vararg
        internal static void Vararg(Instruction i, ref LuaVM vm)
        {
            var (a, b, _) = i.ABC();
            a += 1;

            if (b == 1) return;
            // b==0 or b>1
            vm.LoadVararg(b - 1);
            PopResults(a, b, ref vm);
        }

        // return R(A)(R(A+1), ... ,R(A+B-1))
        internal static void TailCall(Instruction i, ref LuaVM vm)
        {
            var ( a, b, _) = i.ABC();
            a += 1;

            // todo: optimize tail call!
            var c = 0;
            var nArgs = PushFuncAndArgs(a, b, ref vm);
            vm.Call(nArgs, c - 1);
            PopResults(a, c, ref vm);
        }

        // R(A+1) := R(B); R(A) := R(B)[RK(C)]
        internal static void Self(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;
            b += 1;

            vm.Copy(b, a + 1);
            vm.GetRK(c);
            vm.GetTable(b);
            vm.Replace(a);
        }


        internal static void TForCall(Instruction i, ref LuaVM vm)
        {
            var (a, _, c ) = i.ABC();
            a += 1;

            PushFuncAndArgs(a, 3, ref vm);
            vm.Call(2, c);
            PopResults(a + 3, c + 1, ref vm);
        }
    }
}