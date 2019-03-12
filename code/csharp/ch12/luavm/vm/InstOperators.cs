using luavm.api;
using ArithOp = System.Int32;
using CompareOp = System.Int32;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstOperators
    {
        private static void BinaryArith(Instruction i, LuaVM vm, ArithOp op)
        {
            var (a, b, c) = i.ABC();
            a += 1;

            vm.GetRK(b);
            vm.GetRK(c);
            vm.Arith(op);
            vm.Replace(a);
        }

        private static void UnaryArith(Instruction i, LuaVM vm, ArithOp op)
        {
            var (a, b, _) = i.ABC();
            a += 1;
            b += 1;

            vm.PushValue(b);
            vm.Arith(op);
            vm.Replace(a);
        }

        internal static void Add(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPADD);
        }

        internal static void Sub(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPSUB);
        }

        internal static void mul(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPMUL);
        }

        internal static void Mod(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPMOD);
        }

        internal static void pow(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPPOW);
        }

        internal static void Div(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPDIV);
        }

        internal static void Idiv(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPIDIV);
        }

        internal static void Band(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPBAND);
        }

        internal static void Bor(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPBOR);
        }

        internal static void Bxor(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPBXOR);
        }

        internal static void Shl(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPSHL);
        }

        internal static void Shr(Instruction i, ref LuaVM vm)
        {
            BinaryArith(i, vm, Constant.LUA_OPSHR);
        }

        internal static void Unm(Instruction i, ref LuaVM vm)
        {
            UnaryArith(i, vm, Constant.LUA_OPUNM);
        }

        internal static void Bnot(Instruction i, ref LuaVM vm)
        {
            UnaryArith(i, vm, Constant.LUA_OPBNOT);
        }

        internal static void Length(Instruction i, ref LuaVM vm)
        {
            var (a, b, _) = i.ABC();
            a += 1;
            b += 1;

            vm.Len(b);
            vm.Replace(a);
        }

        internal static void Concat(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;
            b += 1;
            c += 1;

            var n = c - b + 1;
            vm.CheckStack(n);
            for (var l = b; l <= c; l++)
            {
                vm.PushValue(l);
            }

            vm.Concat(n);
            vm.Replace(a);
        }

        private static void Compare(Instruction i, ref LuaVM vm, CompareOp op)
        {
            var (a, b, c) = i.ABC();

            vm.GetRK(b);
            vm.GetRK(c);
            var br = vm.Compare(-2, -1, op) != (a != 0);
            if (br)
            {
                vm.AddPC(1);
            }

            vm.Pop(2);
        }

        internal static void Eq(Instruction i, ref LuaVM vm)
        {
            Compare(i, ref vm, Constant.LUA_OPEQ);
        }

        internal static void Lt(Instruction i, ref LuaVM vm)
        {
            Compare(i, ref vm, Constant.LUA_OPLT);
        }

        internal static void Le(Instruction i, ref LuaVM vm)
        {
            Compare(i, ref vm, Constant.LUA_OPLE);
        }

        internal static void Not(Instruction i, ref LuaVM vm)
        {
            var (a, b, _) = i.ABC();
            a += 1;
            b += 1;

            vm.PushBoolean(!vm.ToBoolean(b));
            vm.Replace(a);
        }

        internal static void Test(Instruction i, ref LuaVM vm)
        {
            var (a, _, c) = i.ABC();
            a += 1;
            c += 1;


            if (vm.ToBoolean(a) != (c != 0))
            {
                vm.AddPC(1);
            }
        }

        internal static void TestSet(Instruction i, ref LuaVM vm)
        {
            var (a, b, c) = i.ABC();
            a += 1;
            b += 1;
            c += 1;

            if (vm.ToBoolean(b) == (c != 0))
            {
                vm.Copy(b, a);
            }
            else
            {
                vm.AddPC(1);
            }
        }
    }
}