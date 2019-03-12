using System;
using System.Reflection;
using luavm.api;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public class InstCall
    {
        // R(A) := closure(KPROTO[Bx])
        internal static void closure(Instruction i, ref LuaVM vm)
        {
            var aBx = i.ABx();
            var a = aBx.Item1 + 1;
            var bx = aBx.Item2;

            vm.LoadProto(bx);
            vm.Replace(a);
        }

        // R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
        internal static void call(Instruction i, ref LuaVM vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2;
            var c = abc.Item3;

            // println(":::"+ vm.StackToString())
            var nArgs = _pushFuncAndArgs(a, b, ref vm);
            vm.Call(nArgs, c - 1);
            _popResults(a, c, ref vm);
        }

        static int _pushFuncAndArgs(int a, int b, ref LuaVM vm)
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

            _fixStack(a, vm);
            return vm.GetTop() - vm.RegisterCount() - 1;
        }

        static void _fixStack(int a, LuaVM vm)
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

        static void _popResults(int a, int c, ref LuaVM vm)
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
                vm.PushInteger(Convert.ToInt64(a));
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
                _fixStack(a, vm);
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
                _popResults(a, b, ref vm);
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
            var nArgs = _pushFuncAndArgs(a, b, ref vm);
            vm.Call(nArgs, c - 1);
            _popResults(a, c, ref vm);
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