using System;
using System.Threading;

namespace luavm.state
{
    public struct LuaStack
    {
        private object[] slots;
        internal int top;

        internal static LuaStack newLuaStack(int size)
        {
            return new LuaStack
            {
                slots = new object[size],
                top = 0
            };
        }

        internal void check(int n)
        {
            var free = slots.Length - top;
            if (n <= free) return;
            var newSlots = new object[top + n];
            Array.Copy(slots, newSlots, slots.Length);
            slots = newSlots;
        }

        internal void push(object val)
        {
            if (top == slots.Length)
            {
                throw new Exception("stack overflow!");
            }

            slots[top] = val;

            top++;
        }

        internal int absIndex(int idx)
        {
            if (idx > 0)
            {
                return idx;
            }

            return idx + top + 1;
        }

        internal bool isValid(int idx)
        {
            var absIdx = absIndex(idx);
            return absIdx > 0 && absIdx <= top;
        }

        internal object get(int idx)
        {
            var absIdx = absIndex(idx);
            if (absIdx > 0 && absIdx <= top)
            {
                return slots[absIdx - 1];
            }

            return null;
        }

        internal void setTable(int idx, LuaValue k, LuaValue v)
        {
            var t = get(idx);
            if (new LuaValue(t).isLuaTable())
            {
                var tbl = (LuaTable) t;
                tbl.put(k, v);
                set(idx, tbl);
                return;
            }

            throw new Exception("not a table!");
        }

        internal object pop()
        {
            if (top < 1)
            {
                throw new Exception("stack overflow!");
            }

            top--;
            var val = slots[top];
            slots[top] = null;
            return val;
        }

        internal void set(int idx, object val)
        {
            var absIdx = absIndex(idx);
            if (absIdx <= 0 || absIdx > top) throw new Exception("invalid index!");
            slots[absIdx - 1] = val;
        }

        internal void reverse(int from, int to)
        {
            if (to > from)
            {
                Array.Reverse(slots, from, to - from + 1);
            }
            else if (to < from)
            {
                Array.Reverse(slots, to, from - to + 1);
            }
        }
    }
}