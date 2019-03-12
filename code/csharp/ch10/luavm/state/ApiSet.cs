using System;
using luavm.api;

namespace luavm.state
{
    partial struct LuaState
    {
        public void SetTable(int idx)
        {
            var v = stack.pop();
            var k = stack.pop();
            setTable(idx, k, v);
        }

        void setTable(int idx, object k, object v)
        {
            var t = stack.get(idx);
            if (LuaValue.isLuaTable(t))
            {
                var table = LuaValue.toLuaTable(t);
                table.put(k, v);
                stack.set(idx, table);
                return;
            }

            throw new Exception("not a table!");
        }

        public void SetField(int idx, string k)
        {
            var v = stack.pop();
            setTable(idx, k, v);
        }

        public void SetI(int idx, long n)
        {
            var v = stack.pop();
            setTable(idx, n, v);
        }

        public void SetGlobal(string name)
        {
            var t = registry.get(Consts.LUA_RIDX_GLOBALS);
            var v = stack.pop();
            setTable(ref t, name, v);
            registry.put(Consts.LUA_RIDX_GLOBALS, t);
        }


        // t[k]=v
        void setTable(ref object t, object k, object v)
        {
            if (LuaValue.isLuaTable(t))
            {
                var tbl = LuaValue.toLuaTable(t);
                tbl.put(k, v);
                t = tbl;
                return;
            }

            throw new Exception("not a table!");
        }

        public void Register(string name, CsharpFunction f)
        {
            PushCsharpFunction(f);
            SetGlobal(name);
        }
    }
}