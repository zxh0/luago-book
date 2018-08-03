package com.github.zxh0.luago.state;

import com.github.zxh0.luago.api.LuaState;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LuaStateStackTest {

    private LuaState ls;

    private String lsToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= ls.getTop(); i++) {
            sb.append(ls.toInteger(i));
        }
        return sb.toString();
    }

    @Before
    public void initLuaState() {
        ls = new LuaStateImpl();
        for (int i = 1; i < 10; i++) {
            ls.pushInteger(i);
        }
        assertEquals("123456789", lsToString());
    }

    @Test
    public void stack() {
        ls.copy(8, 3);   assertEquals("128456789",  lsToString());
        ls.pushValue(5); assertEquals("1284567895", lsToString());
        ls.replace(1);   assertEquals("528456789",  lsToString());
        ls.insert(2);    assertEquals("592845678",  lsToString());
        ls.rotate(5, 1); assertEquals("592884567",  lsToString());
        ls.pop(2);       assertEquals("5928845",    lsToString());
        ls.setTop(5);    assertEquals("59288",      lsToString());
    }

}
