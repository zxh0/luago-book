using System;
using luavm.api;
using ArithOp = System.Int32;
using CompareOp = System.Int32;
using Math = luavm.number.Math;

namespace luavm.state
{
    internal delegate long IntegerFunc(long a, long b);

    internal delegate double FloatFunc(double a, double b);

    internal struct Operator
    {
        internal string Metamethod;
        internal IntegerFunc IntegerFunc;
        internal FloatFunc FloatFunc;
    }

    public partial class LuaState
    {
        private static readonly Operator[] Operators =
        {
            new Operator
            {
                Metamethod = "__add",
                IntegerFunc = Iadd,
                FloatFunc = Fadd
            },
            new Operator
            {
                Metamethod = "__sub",
                IntegerFunc = Isub,
                FloatFunc = Fsub
            },
            new Operator
            {
                Metamethod = "__mul",
                IntegerFunc = Imul,
                FloatFunc = Fmul
            },
            new Operator
            {
                Metamethod = "__mod",
                IntegerFunc = Imod,
                FloatFunc = Fmod
            },
            new Operator
            {
                Metamethod = "__pow",
                IntegerFunc = null,
                FloatFunc = Pow
            },
            new Operator
            {
                Metamethod = "__div",
                IntegerFunc = null,
                FloatFunc = Div
            },
            new Operator
            {
                Metamethod = "__idiv",
                IntegerFunc = Iidiv,
                FloatFunc = Fidiv
            },
            new Operator
            {
                Metamethod = "__band",
                IntegerFunc = Band,
                FloatFunc = null
            },
            new Operator
            {
                Metamethod = "__bor",
                IntegerFunc = Bor,
                FloatFunc = null
            },
            new Operator
            {
                Metamethod = "__bxor",
                IntegerFunc = Bxor,
                FloatFunc = null
            },
            new Operator
            {
                Metamethod = "__shl",
                IntegerFunc = Shl,
                FloatFunc = null
            },
            new Operator
            {
                Metamethod = "__shr",
                IntegerFunc = Shr,
                FloatFunc = null
            },
            new Operator
            {
                Metamethod = "__unm",
                IntegerFunc = Inum,
                FloatFunc = Fnum
            },
            new Operator
            {
                Metamethod = "__bnot",
                IntegerFunc = Bnot,
                FloatFunc = null
            }
        };

        private static long Iadd(long a, long b)
        {
            return a + b;
        }

        private static double Fadd(double a, double b)
        {
            return a + b;
        }

        private static long Isub(long a, long b)
        {
            return a - b;
        }

        private static double Fsub(double a, double b)
        {
            return a - b;
        }

        private static long Imul(long a, long b)
        {
            return a * b;
        }

        private static double Fmul(double a, double b)
        {
            return a * b;
        }

        private static long Imod(long a, long b)
        {
            return Math.Mod(a, b);
        }

        private static double Fmod(double a, double b)
        {
            return Math.FMod(a, b);
        }

        private static double Pow(double a, double b)
        {
            return System.Math.Pow(a, b);
        }

        private static double Div(double a, double b)
        {
            return a / b;
        }

        private static long Iidiv(long a, long b)
        {
            return Math.FloorDiv(a, b);
        }

        private static double Fidiv(double a, double b)
        {
            return Math.FFloorDiv(a, b);
        }

        private static long Band(long a, long b)
        {
            return a & b;
        }

        private static long Bor(long a, long b)
        {
            return a | b;
        }

        private static long Bxor(long a, long b)
        {
            return a ^ b;
        }

        private static long Shl(long a, long b)
        {
            return Math.ShiftLeft(a, b);
        }

        private static long Shr(long a, long b)
        {
            return Math.ShiftRight(a, b);
        }

        private static long Inum(long a, long _)
        {
            return -a;
        }

        private static double Fnum(double a, double _)
        {
            return -a;
        }

        private static long Bnot(long a, long _)
        {
            //书上是^a
            return ~a;
        }

        private object GetMetafield(object val, string fieldName, LuaState ls)
        {
            var mt = LuaValue.GetMetatable(val, ls);
            return mt?.Get(fieldName);
        }

        private (object, bool) CallMetamethod(object a, object b, string mmName, LuaState ls)
        {
            var mm = GetMetafield(a, mmName, ls);
            if (mm is null)
            {
                mm = GetMetafield(b, mmName, ls);

                if (mm is null)
                {
                    return (null, false);
                }
            }

            //ls.stack.check(4);
            ls._stack.Push(mm);
            ls._stack.Push(a);
            ls._stack.Push(b);
            ls.Call(2, 1);
            return (ls._stack.Pop(), true);
        }

        public void Arith(ArithOp op)
        {
            var b = _stack.Pop();
            var a = op != Constant.LUA_OPUNM && op != Constant.LUA_OPBNOT ? _stack.Pop() : b;

            var opr = Operators[op];
            var result = arith(a, b, opr);
            if (result != null)
            {
                _stack.Push(result);
                return;
            }

            var mm = opr.Metamethod;
            var (result2, ok) = CallMetamethod(a, b, mm, this);
            if (!ok) throw new Exception("arithmetic error!");
            _stack.Push(result2);
        }

        private static object arith(object a, object b, Operator op)
        {
            if (op.FloatFunc == null)
            {
                var (v,ok1) = LuaValue.ConvertToInteger(a);
                if (!ok1) return null;
                var (v2,ok2) = LuaValue.ConvertToInteger(b);
                if (ok2)
                {
                    return op.IntegerFunc(v, v2);
                }
            }
            else
            {
                if (op.IntegerFunc != null)
                {
                    if (a is long x && b is long y)
                    {
                        return op.IntegerFunc(x, y);
                    }
                }

                var (v,ok1) = LuaValue.ConvertToFloat(a);
                if (!ok1) return null;
                var (v2,ok2) = LuaValue.ConvertToFloat(b);
                if (ok2)
                {
                    return op.FloatFunc(v, v2);
                }
            }

            return null;
        }
    }
}