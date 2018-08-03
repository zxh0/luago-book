package com.github.zxh0.luago.state;

import com.github.zxh0.luago.api.ArithOp;
import com.github.zxh0.luago.api.LuaState;
import org.junit.Test;

import static com.github.zxh0.luago.api.ArithOp.*;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.Assert.assertEquals;

public class LuaStateArithTest {

    @Test
    public void idiv() {
        assertEquals( 1L,  calc( 5L,   3L,  LUA_OPIDIV));
        assertEquals(-2L,  calc(-5L,   3L,  LUA_OPIDIV));
        assertEquals(-2.0, calc( 5L,  -3.0, LUA_OPIDIV));
        assertEquals( 1.0, calc(-5.0, -3.0, LUA_OPIDIV));
    }

    @Test
    public void mod() {
        assertEquals( 2L,  calc( 5L,   3L,  LUA_OPMOD));
        assertEquals( 1L,  calc(-5L,   3L,  LUA_OPMOD));
        assertEquals(-1.0, calc( 5L,  -3.0, LUA_OPMOD));
        assertEquals(-2.0, calc(-5.0, -3.0, LUA_OPMOD));

        assertEquals(2.0,               calc( 2.0, POSITIVE_INFINITY, LUA_OPMOD));
        assertEquals(POSITIVE_INFINITY, calc(-2.0, POSITIVE_INFINITY, LUA_OPMOD));
        assertEquals(-2.0,              calc(-2.0, NEGATIVE_INFINITY, LUA_OPMOD));
        assertEquals(NEGATIVE_INFINITY, calc( 2.0, NEGATIVE_INFINITY, LUA_OPMOD));
    }

    @Test
    public void shift() {
        assertEquals(0b1100L, calc(0b0110L,  1L, LUA_OPSHL));
        assertEquals(0b1100L, calc(0b0110L, -1L, LUA_OPSHR));
        assertEquals(0b0011L, calc(0b0110L, -1L, LUA_OPSHL));
        assertEquals(0b0011L, calc(0b0110L,  1L, LUA_OPSHR));
    }

    private Object calc(Object x, Object y, ArithOp op) {
        LuaState ls = new LuaStateImpl();
        pushOperand(ls, x);
        pushOperand(ls, y);
        ls.arith(op);
        return getResult(ls);
    }

    private void pushOperand(LuaState ls, Object x) {
        if (x instanceof Long) {
            ls.pushInteger((Long) x);
        } else {
            ls.pushNumber((Double) x);
        }
    }

    private Object getResult(LuaState ls) {
        Long i = ls.toIntegerX(1);
        if (i != null) {
            return i;
        }
        return ls.toNumber(1);
    }

}
