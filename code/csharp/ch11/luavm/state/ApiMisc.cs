using System;

namespace luavm.state
{
    public partial class LuaState
    {
        public void Len(int idx)
        {
//            Object val = stack.get(idx);
//            if (val instanceof String) {
//                pushInteger(((String) val).length());
//                return;
//            }
//            Object mm = getMetamethod(val, val, "__len");
//            if (mm != null) {
//                stack.push(callMetamethod(val, val, mm));
//                return;
//            }
//            if (val instanceof LuaTable) {
//                pushInteger(((LuaTable) val).length());
//                return;
//            }
//            throw new RuntimeException("length error!");

            var val = stack.get(idx);
            if (val is string str)
            {
                PushInteger(str.Length);
                return;
            }

            var (result, ok) = callMetamethod(val, val, "__len", this);
            if (ok)
            {
                stack.push(result);
                return;
            }

            if (val is LuaTable table)
            {
                PushInteger(table.len());
                return;
            }

            throw new Exception("length error!");
        }

        public void Concat(int n)
        {
            if (n == 0)
            {
                stack.push("");
            }
            else if (n >= 2)
            {
                for (var i = 1; i < n; i++)
                {
                    if (IsString(-1) && IsString(-2))
                    {
                        var s2 = ToString(-1);
                        var s1 = ToString(-2);
                        stack.pop();
                        stack.pop();
                        stack.push(s1 + s2);
                        continue;
                    }

                    var b = stack.pop();
                    var a = stack.pop();
                    var (result, ok) = callMetamethod(a, b, "__concat", this);
                    if (ok)
                    {
                        stack.push(result);
                        continue;
                    }


                    throw new Exception("concatenation error!");
                }

                // n==1, do nothing
            }
        }

        public uint RawLen(int idx)
        {
            var val = stack.get(idx);
            if (val is string valStr)
            {
                return (uint) valStr.Length;
            }

            if (val is LuaTable luaTable)
            {
                return (uint) (luaTable).len();
            }

            return 0;
        }
    }
}