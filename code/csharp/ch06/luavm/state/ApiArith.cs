using System;
using luavm.api;
using ArithOp = System.Int32;
using CompareOp = System.Int32;

namespace luavm.state
{
    internal delegate long IntegerFunc(long a, long b);

    internal delegate double FloatFunc(double a, double b);

    struct Operator
    {
        public IntegerFunc integerFunc;
        public FloatFunc floatFunc;
    }

    public partial struct LuaState
    {
        private static Operator[] operators = new Operator[]
        {
            new Operator
            {
                integerFunc = iadd,
                floatFunc = fadd
            },
            new Operator
            {
                integerFunc = isub,
                floatFunc = fsub
            },
            new Operator
            {
                integerFunc = imul,
                floatFunc = fmul
            },
            new Operator
            {
                integerFunc = imod,
                floatFunc = fmod
            },
            new Operator
            {
                integerFunc = null,
                floatFunc = pow
            },
            new Operator
            {
                integerFunc = null,
                floatFunc = div
            },
            new Operator
            {
                integerFunc = iidiv,
                floatFunc = fidiv
            },
            new Operator
            {
                integerFunc = band,
                floatFunc = null
            },
            new Operator
            {
                integerFunc = bor,
                floatFunc = null
            },
            new Operator
            {
                integerFunc = bxor,
                floatFunc = null
            },
            new Operator
            {
                integerFunc = shl,
                floatFunc = null
            },
            new Operator
            {
                integerFunc = shr,
                floatFunc = null
            },
            new Operator
            {
                integerFunc = inum,
                floatFunc = fnum
            },
            new Operator
            {
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
            return number.Math.IMod(a, b);
        }

        private static double fmod(double a, double b)
        {
            return number.Math.FMod(a, b);
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
            return number.Math.IFloorDiv(a, b);
        }

        private static double fidiv(double a, double b)
        {
            return number.Math.FFloorDiv(a, b);
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
            return number.Math.ShiftLeft(a, b);
        }

        private static long shr(long a, long b)
        {
            return number.Math.ShiftRight(a, b);
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


        public void Arith(ArithOp op)
        {
            LuaValue a, b;
            b = new LuaValue(stack.pop());
            if (op != Consts.LUA_OPUNM && op != Consts.LUA_OPBNOT)
            {
                a = new LuaValue(stack.pop());
            }
            else
            {
                a = b;
            }

            var opr = operators[op];
            var result = _arith(a, b, opr);
            if (result != null)
            {
                stack.push(result);
            }
            else
            {
                throw new Exception("arithmetic error!");
            }
        }

        object _arith(LuaValue a, LuaValue b, Operator op)
        {
            if (op.floatFunc == null)
            {
                Tuple<long, bool> v = LuaValue.convertToInteger(a.value);
                if (v.Item2)
                {
                    Tuple<long, bool> v2 = LuaValue.convertToInteger(b.value);
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
                    if (a.value.GetType().Name.Equals("Int64") && b.value.GetType().Name.Equals("Int64"))
                    {
                        var x = long.Parse(a.value.ToString());
                        var y = long.Parse(b.value.ToString());
                        return op.integerFunc(x, y);
                    }
                }

                var v = LuaValue.convertToFloat(a.value);
                if (v.Item2)
                {
                    var v2 = LuaValue.convertToFloat(b.value);
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