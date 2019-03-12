using System;

namespace luavm.state
{
    public partial struct LuaState
    {
        public void Len(int idx)
        {
            var val = stack.get(idx);
            if (LuaValue.isString(val))
            {
                var s = LuaValue.toString(val);
                stack.push((long) s.Length);
            }
            else if (LuaValue.isLuaTable(val))
            {
                var t = LuaValue.toLuaTable(val);
                stack.push((long) t.len());
            }
            else
            {
                throw new Exception("length error!");
            }
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

                    throw new Exception("concatenation error!");
                }

                // n==1, do nothing
            }
        }
    }
}