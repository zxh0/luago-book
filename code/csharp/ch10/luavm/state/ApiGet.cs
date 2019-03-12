using System;
using luavm.api;
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
            var t = stack.get(idx);
            var k = stack.pop();
            return getTable(t, k);
        }

        LuaType getTable(object t, object k)
        {
            if (LuaValue.isLuaTable(t))
            {
                var tbl = LuaValue.toLuaTable(t);
                var v = tbl.get(k);
                stack.push(v);
                return LuaValue.typeOf(v);
            }

            throw new Exception("not a table!");
        }

        public LuaType GetField(int idx, string k)
        {
            var t = stack.get(idx);
            return getTable(t, k);
//            PushString(k);
//            return GetTable(idx);
        }

        public LuaType GetI(int idx, long i)
        {
            var t = stack.get(idx);
            return getTable(t, i);
        }

        public int GetGlobal(string name)
        {
            var t = registry.get(Consts.LUA_RIDX_GLOBALS);
            return getTable(t, name);
        }
    }
}