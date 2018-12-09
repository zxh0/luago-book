using luavm.api;
using ArithOp = System.Int32;
using CompareOp = System.Int32;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public static class InstOperators
    {
        private static void _binaryArith(Instruction i, LuaVM vm, ArithOp op)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2;
            var c = abc.Item3;

            vm.GetRK(b);
            vm.GetRK(c);
            vm.Arith(op);
            vm.Replace(a);
        }

        private static void _unaryArith(Instruction i, LuaVM vm, ArithOp op)
        {
            var ab_ = i.ABC();
            var a = ab_.Item1 + 1;
            var b = ab_.Item2 + 1;

            vm.PushValue(b);
            vm.Arith(op);
            vm.Replace(a);
        }

        internal static void add(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPADD);
        }

        internal static void sub(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPSUB);
        }

        internal static void mul(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPMUL);
        }

        internal static void mod(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPMOD);
        }

        internal static void pow(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPPOW);
        }

        internal static void div(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPDIV);
        }

        internal static void idiv(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPIDIV);
        }

        internal static void band(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPBAND);
        }

        internal static void bor(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPBOR);
        }

        internal static void bxor(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPBXOR);
        }

        internal static void shl(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPSHL);
        }

        internal static void shr(Instruction i, ref LuaVM vm)
        {
            _binaryArith(i, vm, Consts.LUA_OPSHR);
        }

        internal static void unm(Instruction i, ref LuaVM vm)
        {
            _unaryArith(i, vm, Consts.LUA_OPUNM);
        }

        internal static void bnot(Instruction i, ref LuaVM vm)
        {
            _unaryArith(i, vm, Consts.LUA_OPBNOT);
        }

        internal static void length(Instruction i, ref LuaVM vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2 + 1;

            vm.Len(b);
            vm.Replace(a);
        }

        internal static void concat(Instruction i, ref LuaVM vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2 + 1;
            var c = abc.Item3 + 1;

            var n = c - b + 1;
            vm.CheckStack(n);
            for (var l = b; l <= c; l++)
            {
                vm.PushValue(l);
            }

            vm.Concat(n);
            vm.Replace(a);
        }

        internal static void _compare(Instruction i, ref LuaVM vm, CompareOp op)
        {
            var abc = i.ABC();
            var a = abc.Item1;
            var b = abc.Item2;
            var c = abc.Item3;

            vm.GetRK(b);
            vm.GetRK(c);
            if (vm.Compare(-2, -1, op) != (a != 0))
            {
                vm.AddPC(1);
            }

            vm.Pop(2);
        }

        internal static void eq(Instruction i, ref LuaVM vm)
        {
            _compare(i, ref vm, Consts.LUA_OPEQ);
        }

        internal static void lt(Instruction i, ref LuaVM vm)
        {
            _compare(i, ref vm, Consts.LUA_OPLT);
        }

        internal static void le(Instruction i, ref LuaVM vm)
        {
            _compare(i, ref vm, Consts.LUA_OPLE);
        }

        internal static void not(Instruction i, ref LuaVM vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2 + 1;
            var c = abc.Item3 + 1;

            vm.PushBoolean(!vm.ToBoolean(b));
            vm.Replace(a);
        }

        internal static void test(Instruction i, ref LuaVM vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2 + 1;
            var c = abc.Item3 + 1;

            if (vm.ToBoolean(a) != (c != 0))
            {
                vm.AddPC(1);
            }
        }

        internal static void testSet(Instruction i, ref LuaVM vm)
        {
            var abc = i.ABC();
            var a = abc.Item1 + 1;
            var b = abc.Item2 + 1;
            var c = abc.Item3 + 1;

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