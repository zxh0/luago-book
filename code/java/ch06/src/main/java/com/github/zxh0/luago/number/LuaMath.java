package com.github.zxh0.luago.number;

public class LuaMath {

    public static double floorDiv(double a, double b) {
        return Math.floor(a / b);
    }

    // a % b == a - ((a // b) * b)
    public static double floorMod(double a, double b) {
        if (a > 0 && b == Double.POSITIVE_INFINITY
                || a < 0 && b == Double.NEGATIVE_INFINITY) {
            return a;
        }
        if (a > 0 && b == Double.NEGATIVE_INFINITY
                || a < 0 && b == Double.POSITIVE_INFINITY) {
            return b;
        }
        return a - Math.floor(a / b) * b;
    }

    public static long shiftLeft(long a, long n) {
        return n >= 0 ? a << n : a >>> -n;
    }

    public static long shiftRight(long a, long n) {
        return n >= 0 ? a >>> n : a << -n;
    }

}
