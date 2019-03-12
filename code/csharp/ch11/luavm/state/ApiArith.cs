using System;
using luavm.api;
using ArithOp = System.Int32;
using CompareOp = System.Int32;
using Math = luavm.number.Math;

namespace luavm.state
{
    internal delegate long IntegerFunc(long a, long b);

    internal delegate double FloatFunc(double a, double b);

    struct Operator
    {
        internal string metamethod;
        internal IntegerFunc integerFunc;
        internal FloatFunc floatFunc;
    }

    public partial class LuaState
    {
        private static Operator[] operators = {
            new Operator
            {
                metamethod = "__add",
                integerFunc = iadd,
                floatFunc = fadd
            },
            new Operator
            {
                metamethod = "__sub",
                integerFunc = isub,
                floatFunc = fsub
            },
            new Operator
            {
                metamethod = "__mul",
                integerFunc = imul,
                floatFunc = fmul
            },
            new Operator
            {
                metamethod = "__mod",
                integerFunc = imod,
                floatFunc = fmod
            },
            new Operator
            {
                metamethod = "__pow",
                integerFunc = null,
                floatFunc = pow
            },
            new Operator
            {
                metamethod = "__div",
                integerFunc = null,
                floatFunc = div
            },
            new Operator
            {
                metamethod = "__idiv",
                integerFunc = iidiv,
                floatFunc = fidiv
            },
            new Operator
            {
                metamethod = "__band",
                integerFunc = band,
                floatFunc = null
            },
            new Operator
            {
                metamethod = "__bor",
                integerFunc = bor,
                floatFunc = null
            },
            new Operator
            {
                metamethod = "__bxor",
                integerFunc = bxor,
                floatFunc = null
            },
            new Operator
            {
                metamethod = "__shl",
                integerFunc = shl,
                floatFunc = null
            },
            new Operator
            {
                metamethod = "__shr",
                integerFunc = shr,
                floatFunc = null
            },
            new Operator
            {
                metamethod = "__unm",
                integerFunc = inum,
                floatFunc = fnum
            },
            new Operator
            {
                metamethod = "__bnot",
                integerFunc = bnot,
                floatFunc = null
            }
        };

        private static long iadd(long a, long b)
        {
            return a + b;
        }

        private static double fadd(double a, double b)
        {
            return a + b;
        }

        private static long isub(long a, long b)
        {
            return a - b;
        }

        private static double fsub(double a, double b)
        {
            return a - b;
        }

        private static long imul(long a, long b)
        {
            return a * b;
        }

        private static double fmul(double a, double b)
        {
            return a * b;
        }

        private static long imod(long a, long b)
        {
            return Math.IMod(a, b);
        }

        private static double fmod(double a, double b)
        {
            return Math.FMod(a, b);
        }

        private static double pow(double a, double b)
        {
            return System.Math.Pow(a, b);
        }

        private static double div(double a, double b)
        {
            return a / b;
        }

        private static long iidiv(long a, long b)
        {
            return Math.IFloorDiv(a, b);
        }

        private static double fidiv(double a, double b)
        {
            return Math.FFloorDiv(a, b);
        }

        private static long band(long a, long b)
        {
            return a & b;
        }

        private static long bor(long a, long b)
        {
            return a | b;
        }

        private static long bxor(long a, long b)
        {
            return a ^ b;
        }

        private static long shl(long a, long b)
        {
            return Math.ShiftLeft(a, b);
        }

        private static long shr(long a, long b)
        {
            return Math.ShiftRight(a, b);
        }

        private static long inum(long a, long _)
        {
            return -a;
        }

        private static double fnum(double a, double _)
        {
            return -a;
        }

        private static long bnot(long a, long _)
        {
            //书上是^a
            return ~a;
        }

        public object getMetafield(object val, string fieldName, LuaState ls)
        {
            var mt = LuaValue.getMetatable(val, ls);
            return mt?.get(fieldName);
        }

        public (object, bool) callMetamethod(object a, object b, string mmName, LuaState ls)
        {
            var mm = getMetafield(a, mmName, ls);
            if (mm is null)
            {
                mm = getMetafield(b, mmName, ls);

                if (mm is null)
                {
                    return (null, false);
                }
            }

            //ls.stack.check(4);
            ls.stack.push(mm);
            ls.stack.push(a);
            ls.stack.push(b);
            ls.Call(2, 1);
            return (ls.stack.pop(), true);
        }

        public void Arith(ArithOp op)
        {
            object b = stack.pop();
            object a = op != Consts.LUA_OPUNM && op !=  Consts.LUA_OPBNOT ? stack.pop() : b;

            var opr = operators[op];
            var result = _arith(a, b, opr);
            if (result != null)
            {
                stack.push(result);
                return;
            }

            var mm = opr.metamethod;
            var (result2, ok) = callMetamethod(a, b, mm, this);
            if (ok)
            {
                stack.push(result2);
                return;
            }

            throw new Exception("arithmetic error!");
        }

        object _arith(object a, object b, Operator op)
        {
            if (op.floatFunc == null)
            {
                var v = LuaValue.convertToInteger(a);
                if (v.Item2)
                {
                    var v2 = LuaValue.convertToInteger(b);
                    if (v2.Item2)
                    {
                        return op.integerFunc(v.Item1, v2.Item1);
                    }
                }
            }
            else
            {
                if (op.integerFunc != null)
                {
                    if (LuaValue.isInteger(a) && LuaValue.isInteger(b))
                    {
                        var x = LuaValue.toInteger(a);
                        var y = LuaValue.toInteger(b);
                        return op.integerFunc(x, y);
                    }
                }

                var v = LuaValue.convertToFloat(a);
                if (v.Item2)
                {
                    var v2 = LuaValue.convertToFloat(b);
                    if (v2.Item2)
                    {
                        return op.floatFunc(v.Item1, v2.Item1);
                    }
                }
            }

            return null;
        }
    }
}