using System;
using luavm.api;

namespace luavm.state
{
    partial class LuaState
    {
        public void SetTable(int idx)
        {
            var t = _stack.Get(idx);
            var v = _stack.Pop();
            var k = _stack.Pop();
            setTable(t, k, v, false);
        }

        public void SetField(int idx, string k)
        {
            var t = _stack.Get(idx);
            var v = _stack.Pop();
            setTable(t, k, v, false);
        }

        public void SetI(int idx, long i)
        {
            var t = _stack.Get(idx);
            var v = _stack.Pop();
            setTable(t, i, v, false);
        }

        public void SetGlobal(string name)
        {
            var t = Registry.Get(Constant.LUA_RIDX_GLOBALS);
            var v = _stack.Pop();
            setTable(t, name, v, false);
        }


        // t[k]=v
        void setTable(object t, object k, object v, bool raw)
        {
            while (true)
            {
                if (t is LuaTable tbl)
                {
                    if (raw || tbl.Get(k) != null || !tbl.HasMetafield("__newindex"))
                    {
                        tbl.Put(k, v);
                        return;
                    }
                }

                if (raw) throw new Exception("index error!");
                var mf = GetMetafield(t, "__newindex", this);
                if (mf == null) throw new Exception("index error!");
                switch (mf)
                {
                    case LuaTable _:
                        t = mf;
                        raw = false;
                        continue;
                    case Closure _:
                        _stack.Push(mf);
                        _stack.Push(t);
                        _stack.Push(k);
                        _stack.Push(v);
                        Call(3, 0);
                        return;
                }

                throw new Exception("index error!");
            }
        }

        public void Register(string name, CsharpFunction f)
        {
            PushCsharpFunction(f);
            SetGlobal(name);
        }

        public void SetMetatable(int idx)
        {
            var val = _stack.Get(idx);
            var mtVal = _stack.Pop();

            switch (mtVal)
            {
                case null:
                    LuaValue.SetMetatable(val, null, this);
                    break;
                case LuaTable mtl:
                    LuaValue.SetMetatable(val, mtl, this);
                    break;
                default:
                    throw new Exception("table expected!"); // todo
            }
        }

        public void RawSet(int idx)
        {
            var t = _stack.Get(idx);
            var v = _stack.Pop();
            var k = _stack.Pop();
            setTable(t, k, v, true);
        }

        public void RawSetI(int idx, long i)
        {
            var t = _stack.Get(idx);
            var v = _stack.Pop();
            setTable(t, i, v, true);
        }
    }
}