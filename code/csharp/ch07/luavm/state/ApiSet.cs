using System;

namespace luavm.state
{
    partial struct LuaState
    {
        public void SetTable(int idx)
        {
            var v = stack.pop();
            var k = stack.pop();
            stack.setTable(idx, new LuaValue(k), new LuaValue(v));
        }

//        internal LuaTable setTable(object t, LuaValue k, LuaValue v)
//        {
//            if (new LuaValue(t).isLuaTable())
//            {
//                var tbl = (LuaTable) t;
//                tbl.put(k, v);
//                return tbl;
//            }
//
//            throw new Exception("not a table!");
//        }

        public void SetField(int idx, string k)
        {
            var t = stack.get(idx);
            var v = stack.pop();
            stack.setTable(idx, new LuaValue(k), new LuaValue(v));
        }

        public void SetI(int idx, long n)
        {
            var t = stack.get(idx);
            var v = stack.pop();
            stack.setTable(idx, new LuaValue(n), new LuaValue(v));
        }
    }
}