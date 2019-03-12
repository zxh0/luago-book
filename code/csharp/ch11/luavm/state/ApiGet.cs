using System;
using luavm.api;
using LuaType = System.Int32;

namespace luavm.state
{
    partial class LuaState
    {
        public void NewTable()
        {
            CreateTable(0, 0);
        }

        public void CreateTable(int nArr, int nRec)
        {
            stack.push(new LuaTable(nArr, nRec));
        }

        public int GetTable(int idx)
        {
            var t = stack.get(idx);
            var k = stack.pop();
            return getTable(t, k, false);
        }

        LuaType getTable(object t, object k, bool raw)
        {
            if (t is LuaTable luaTable)
            {
                var v = luaTable.get(k);
                if (raw || v != null || !luaTable.hasMetafield("__index"))
                {
                    stack.push(v);
                    return LuaValue.typeOf(v);
                }
            }

            if (!raw)
            {
                var mf = getMetafield(t, "__index", this);
                if (mf != null)
                {
                    switch (mf)
                    {
                        case LuaTable mfTable:
                            getTable(mfTable, k, false);
                            break;
                        case Closure closure:
                            stack.push(closure);
                            stack.push(t);
                            stack.push(k);
                            Call(2, 1);
                            var v = stack.get(-1);
                            return LuaValue.typeOf(v);
                    }
                }
            }

            throw new Exception("not a table!");
        }

        public LuaType GetField(int idx, string k)
        {
            var t = stack.get(idx);
            return getTable(t, k, false);
//            PushString(k);
//            return GetTable(idx);
        }

        public LuaType GetI(int idx, long i)
        {
            var t = stack.get(idx);
            return getTable(t, i, false);
        }

        public int GetGlobal(string name)
        {
            var t = registry.get(Consts.LUA_RIDX_GLOBALS);
            return getTable(t, name, false);
        }

        public bool GetMetatable(int idx)
        {
            var val = stack.get(idx);
            var mt = LuaValue.getMetatable(val, this);
            if (mt != null)
            {
                stack.push(mt);
                return true;
            }

            return false;
        }

        public int RawGet(int idx)
        {
            var t = stack.get(idx);
            var k = stack.pop();
            return getTable(t, k, true);
        }

        public int RawGetI(int idx, long i)
        {
            var t = stack.get(idx);
            return getTable(t, i, true);
        }
    }
}