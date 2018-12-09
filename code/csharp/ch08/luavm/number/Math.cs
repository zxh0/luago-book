using System;

namespace luavm.number
{
    public class Math
    {
        internal static long IFloorDiv(long a, long b)
        {
            if (a > 0 && b > 0 || a < 0 && b < 0 || a % b == 0)
            {
                return a / b;
            }

            return a / b - 1;
        }

        internal static double FFloorDiv(double a, double b)
        {
            return System.Math.Floor(a / b);
        }

        internal static long IMod(long a, long b)
        {
            return a - IFloorDiv(a, b) * b;
        }

        internal static double FMod(double a, double b)
        {
            return a - System.Math.Floor(a / b) * b;
        }

        internal static long ShiftLeft(long a, long n)
        {
            if (n >= 0)
            {
                return a << (int) n;
            }

            return ShiftRight(a, (int) -n);
        }

        internal static long ShiftRight(long a, long n)
        {
            if (n >= 0)
            {
                return a >> (int) n;
            }

            return ShiftLeft(a, -n);
        }

        internal static Tuple<long, bool> FloatToInteger(double f)
        {
            var i = (long) f;
            return Tuple.Create(i, System.Math.Abs(i - f) <= 0);
        }
    }
}