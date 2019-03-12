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
            _stack.Push(new LuaTable(nArr, nRec));
        }

        public LuaType GetTable(int idx)
        {
            var t = _stack.Get(idx);
            var k = _stack.Pop();
            return GetTable(t, k, false);
        }

        private LuaType GetTable(object t, object k, bool raw)
        {
            if (t is LuaTable luaTable)
            {
                var v = luaTable.Get(k);
                if (raw || v != null || !luaTable.HasMetafield("__index"))
                {
                    _stack.Push(v);
                    return LuaValue.TypeOf(v);
                }
            }

            if (raw) throw new Exception("not a table!");
            {
                var mf = GetMetafield(t, "__index", this);
                if (mf == null) throw new Exception("not a table!");
                switch (mf)
                {
                    case LuaTable mfTable:
                        GetTable(mfTable, k, false);
                        break;
                    case Closure closure:
                        _stack.Push(closure);
                        _stack.Push(t);
                        _stack.Push(k);
                        Call(2, 1);
                        var v = _stack.Get(-1);
                        return LuaValue.TypeOf(v);
                }
            }

            throw new Exception("not a table!");
        }

        public LuaType GetField(int idx, string k)
        {
            var t = _stack.Get(idx);
            return GetTable(t, k, false);
//            PushString(k);
//            return GetTable(idx);
        }

        public LuaType GetI(int idx, long i)
        {
            var t = _stack.Get(idx);
            return GetTable(t, i, false);
        }

        public int GetGlobal(string name)
        {
            var t = Registry.Get(Constant.LUA_RIDX_GLOBALS);
            return GetTable(t, name, false);
        }

        public bool GetMetatable(int idx)
        {
            var val = _stack.Get(idx);
            var mt = LuaValue.GetMetatable(val, this);
            if (mt == null) return false;
            _stack.Push(mt);
            return true;
        }

        public int RawGet(int idx)
        {
            var t = _stack.Get(idx);
            var k = _stack.Pop();
            return GetTable(t, k, true);
        }

        public int RawGetI(int idx, long i)
        {
            var t = _stack.Get(idx);
            return GetTable(t, i, true);
        }
    }
}