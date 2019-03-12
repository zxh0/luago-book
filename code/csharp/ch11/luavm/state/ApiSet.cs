using System;
using luavm.api;

namespace luavm.state
{
    partial class LuaState
    {
        public void SetTable(int idx)
        {
            var t = stack.get(idx);
            var v = stack.pop();
            var k = stack.pop();
            setTable(t, k, v, false);
        }

        public void SetField(int idx, string k)
        {
            var t = stack.get(idx);
            var v = stack.pop();
            setTable(t, k, v, false);
        }

        public void SetI(int idx, long i)
        {
            var t = stack.get(idx);
            var v = stack.pop();
            setTable(t, i, v, false);
        }

        public void SetGlobal(string name)
        {
            var t = registry.get(Consts.LUA_RIDX_GLOBALS);
            var v = stack.pop();
            setTable(t, name, v, false);
        }


        // t[k]=v
        void setTable(object t, object k, object v, bool raw)
        {
            if (t is LuaTable tbl)
            {
                if (raw || tbl.get(k) != null || !tbl.hasMetafield("__newindex"))
                {
                    tbl.put(k, v);
                    return;
                }
            }

            if (!raw)
            {
                var mf = getMetafield(t, "__newindex", this);
                if (mf != null)
                {
                    switch (mf)
                    {
                        case LuaTable _:
                            setTable(mf, k, v, false);
                            return;
                        case Closure _:
                            stack.push(mf);
                            stack.push(t);
                            stack.push(k);
                            stack.push(v);
                            Call(3, 0);
                            return;
                    }
                }
            }

            throw new Exception("index error!");
        }

        public void Register(string name, CsharpFunction f)
        {
            PushCsharpFunction(f);
            SetGlobal(name);
        }

        public void SetMetatable(int idx)
        {
            var val = stack.get(idx);
            var mtVal = stack.pop();

            if (mtVal == null)
            {
                LuaValue.setMetatable(val, null, this);
            }
            else if (mtVal is LuaTable mtl)
            {
                LuaValue.setMetatable(val, mtl, this);
            }
            else
            {
                throw new Exception("table expected!"); // todo
            }
        }

        public void RawSet(int idx)
        {
            var t = stack.get(idx);
            var v = stack.pop();
            var k = stack.pop();
            setTable(t, k, v, true);
        }

        public void RawSetI(int idx, long i)
        {
            var t = stack.get(idx);
            var v = stack.pop();
            setTable(t, i, v, true);
        }
    }
}