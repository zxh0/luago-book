package com.github.zxh0.luago.state;

import com.github.zxh0.luago.api.ArithOp;
import com.github.zxh0.luago.number.LuaMath;

import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

class Arithmetic {

    private static final LongBinaryOperator[] integerOps = {
            (a, b) -> a + b,     // LUA_OPADD
            (a, b) -> a - b,     // LUA_OPSUB
            (a, b) -> a * b,     // LUA_OPMUL
            Math::floorMod,      // LUA_OPMOD
            null,                // LUA_OPPOW
            null,                // LUA_OPDIV
            Math::floorDiv,      // LUA_OPIDIV
            (a, b) -> a & b,     // LUA_OPBAND
            (a, b) -> a | b,     // LUA_OPBOR
            (a, b) -> a ^ b,     // LUA_OPBXOR
            LuaMath::shiftLeft,  // LUA_OPSHL
            LuaMath::shiftRight, // LUA_OPSHR
            (a, b) -> -a,        // LUA_OPUNM
            (a, b) -> ~a,        // LUA_OPBNOT
    };

    private static final DoubleBinaryOperator[] floatOps = {
            (a, b) -> a + b,   // LUA_OPADD
            (a, b) -> a - b,   // LUA_OPSUB
            (a, b) -> a * b,   // LUA_OPMUL
            LuaMath::floorMod, // LUA_OPMOD
            Math::pow,         // LUA_OPPOW
            (a, b) -> a / b,   // LUA_OPDIV
            LuaMath::floorDiv, // LUA_OPIDIV
            null,              // LUA_OPBAND
            null,              // LUA_OPBOR
            null,              // LUA_OPBXOR
            null,              // LUA_OPSHL
            null,              // LUA_OPSHR
            (a, b) -> -a,      // LUA_OPUNM
            null,              // LUA_OPBNOT
    };

    static Object arith(Object a, Object b, ArithOp op) {
        LongBinaryOperator integerFunc = integerOps[op.ordinal()];
        DoubleBinaryOperator floatFunc = floatOps[op.ordinal()];

        if (floatFunc == null) { // bitwise
            Long x = LuaValue.toInteger(a);
            if (x != null) {
                Long y = LuaValue.toInteger(b);
                if (y != null) {
                    return integerFunc.applyAsLong(x, y);
                }
            }
        } else { // arith
            if (integerFunc != null) { // add,sub,mul,mod,idiv,unm
                if (a instanceof Long && b instanceof Long) {
                    return integerFunc.applyAsLong((Long) a, (Long) b);
                }
            }
            Double x = LuaValue.toFloat(a);
            if (x != null) {
                Double y = LuaValue.toFloat(b);
                if (y != null) {
                    return floatFunc.applyAsDouble(x, y);
                }
            }
        }
        return null;
    }

}
