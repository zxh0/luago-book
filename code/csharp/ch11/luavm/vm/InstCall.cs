using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public class InstCall
    {
        // R(A) := closure(KPROTO[Bx])
        internal static void closure(Instruction i, ref LuaVM vm)
        {
            var (a, Bx) = i.ABx();
            a += 1;

            vm.LoadProto(Bx);
            vm.Replace(a);
        }

        // R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
        internal static void call(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;
            // println(":::"+ vm.StackToString())
            var nArgs = pushFuncAndArgs(a, b, ref vm);
            vm.Call(nArgs, c - 1);
            popResults(a, c, vm);
        }

        static int pushFuncAndArgs(int a, int b, ref LuaVM vm)
        {
            if (b >= 1)
            {
                vm.CheckStack(b);
                for (int i = a; i < a + b; i++)
                {
                    vm.PushValue(i);
                }

                return b - 1;
            }

            fixStack(a, vm);
            return vm.GetTop() - vm.RegisterCount() - 1;
        }

        static void fixStack(int a, LuaVM vm)
        {
            int x = (int) vm.ToInteger(-1);
            vm.Pop(1);

            vm.CheckStack(x - a);
            for (int i = a; i < x; i++) {
                vm.PushValue(i);
            }
            vm.Rotate(vm.RegisterCount()+1, x-a);
        }

        static void popResults(int a, int c, LuaVM vm)
        {
            if (c == 1) {
                // no results
            } else if (c > 1) {
                for (int i = a + c - 2; i >= a; i--) {
                    vm.Replace(i);
                }
            } else {
                // leave results on stack
                vm.CheckStack(1);
                vm.PushInteger(a);
            }
        }

        // return R(A), ... ,R(A+B-2)
        internal static void _return(Instruction i, ref LuaVM vm)
        {
            var ab_ = i.ABC();
            var a = ab_.Item1 + 1;
            var b = ab_.Item2;

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
                fixStack(a, vm);
            }
        }

        // R(A), R(A+1), ..., R(A+B-2) = vararg
        internal static void vararg(Instruction i, ref LuaVM vm)
        {
            var ab_ = i.ABC();
            var a = ab_.Item1 + 1;
            var b = ab_.Item2;

            if (b != 1)
            {
                // b==0 or b>1
                vm.LoadVararg(b - 1);
                popResults(a, b, vm);
            }
        }

        // return R(A)(R(A+1), ... ,R(A+B-1))
        internal static void tailCall(Instruction i, ref LuaVM vm)
        {
            var ab_ = i.ABC();
            var a = ab_.Item1 + 1;
            var b = ab_.Item2;

            // todo: optimize tail call!
            var c = 0;
            var nArgs = pushFuncAndArgs(a, b, ref vm);
            vm.Call(nArgs, c - 1);
            popResults(a, c, vm);
        }

        // R(A+1) := R(B); R(A) := R(B)[RK(C)]
        internal static void self(Instruction i, ref LuaVM vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2 + 1;
            var c = abc.Item3;

            vm.Copy(b, a + 1);
            vm.GetRK(c);
            vm.GetTable(b);
            vm.Replace(a);
        }
    }
}