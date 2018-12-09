using System;

namespace luavm.state
{
    partial struct LuaState
    {
        public void SetTable(int idx)
        {
            var v = stack.pop();
            var k = stack.pop();
            setTable(idx, new LuaValue(k), new LuaValue(v));
        }

        void setTable(int idx, LuaValue k, LuaValue v)
        {
            var t = new LuaValue(stack.get(idx));
            if (t.isLuaTable())
            {
                var table = t.toLuaTable();
                table.put(k, v);
                stack.set(idx, table);
                return;
            }

            throw new Exception("not a table!");
        }

        public void SetField(int idx, string k)
        {
            var v = stack.pop();
            setTable(idx, new LuaValue(k), new LuaValue(v));
        }

        public void SetI(int idx, long n)
        {
            var v = stack.pop();
            setTable(idx, new LuaValue(n), new LuaValue(v));
        }
    }
}