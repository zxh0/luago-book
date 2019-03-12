using System;

namespace luavm.state
{
    public partial class LuaState
    {
        public void Len(int idx)
        {
            var val = _stack.Get(idx);
            if (val is string str)
            {
                PushInteger(str.Length);
                return;
            }

            var (result, ok) = CallMetamethod(val, val, "__len", this);
            if (ok)
            {
                _stack.Push(result);
                return;
            }

            if (!(val is LuaTable table)) throw new Exception("length error!");
            PushInteger(table.Len());
            return;
        }

        public void Concat(int n)
        {
            if (n == 0)
            {
                _stack.Push("");
            }
            else if (n >= 2)
            {
                for (var i = 1; i < n; i++)
                {
                    if (IsString(-1) && IsString(-2))
                    {
                        var s2 = ToString(-1);
                        var s1 = ToString(-2);
                        _stack.Pop();
                        _stack.Pop();
                        _stack.Push(string.Concat(s1, s2));
                        continue;
                    }

                    var b = _stack.Pop();
                    var a = _stack.Pop();
                    var (result, ok) = CallMetamethod(a, b, "__concat", this);
                    if (ok)
                    {
                        _stack.Push(result);
                        continue;
                    }


                    throw new Exception("concatenation error!");
                }

                // n==1, do nothing
            }
        }

        public uint RawLen(int idx)
        {
            var val = _stack.Get(idx);
            switch (val)
            {
                case string valStr:
                    return (uint) valStr.Length;
                case LuaTable luaTable:
                    return (uint) (luaTable).Len();
                default:
                    return 0;
            }
        }

        public bool Next(int idx)
        {
            var val = _stack.Get(idx);
            if (!(val is LuaTable t)) throw new Exception("table expected!");
            var key = _stack.Pop();
            var nextKey = t.NextKey(key);
            if (nextKey == null) return false;
            _stack.Push(nextKey);
            _stack.Push(t.Get(nextKey));
            return true;
        }

        public int Error()
        {
            var err = _stack.Pop();
            throw new Exception(Convert.ToString(err));
        }
    }
}