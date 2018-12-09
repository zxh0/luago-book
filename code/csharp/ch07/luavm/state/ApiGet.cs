using System;
using LuaType = System.Int32;

namespace luavm.state
{
    partial struct LuaState
    {
        public void NewTable()
        {
            CreateTable(0, 0);
        }

        public void CreateTable(int nArr, int nRec)
        {
            var t = LuaTable.newLuaTable(nArr, nRec);
            stack.push(t);
        }

        public int GetTable(int idx)
        {
            var t = new LuaValue(stack.get(idx)).toLuaTable();
            var k = stack.pop();
            return getTable(new LuaValue(t), new LuaValue(k));
        }

        LuaType getTable(LuaValue t, LuaValue k)
        {
            if (t.isLuaTable())
            {
                var tbl = t.toLuaTable();
                var v = tbl.get(k).value;
                if (v.GetType().IsEquivalentTo(typeof(LuaValue)))
                {
                    v = ((LuaValue) v).value;
                }

                stack.push(v);
                return new LuaValue(v).typeOf();
            }

            throw new Exception("not a table!");
        }

        public LuaType GetField(int idx, string k)
        {
            var t = new LuaValue(stack.get(idx)).toLuaTable();
            return getTable(new LuaValue(t), new LuaValue(k));
//            PushString(k);
//            return GetTable(idx);
        }

        public LuaType GetI(int idx, long i)
        {
            var t = new LuaValue(stack.get(idx)).toLuaTable();
            return getTable(new LuaValue(t), new LuaValue(i));
        }
    }
}