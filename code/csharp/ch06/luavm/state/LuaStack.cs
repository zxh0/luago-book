using System;
using System.Threading;

namespace luavm.state
{
    public struct LuaStack
    {
        private LuaValue[] slots;
        internal int top;

        internal static LuaStack newLuaStack(int size)
        {
            return new LuaStack
            {
                slots = new LuaValue[size],
                top = 0
            };
        }

        internal void check(int n)
        {
            var free = slots.Length - top;
            if (n <= free) return;
            var newSlots = new LuaValue[top + n];
            Array.Copy(slots, newSlots, slots.Length);
            slots = newSlots;
        }

        internal void push(object val)
        {
            if (top == slots.Length)
            {
                throw new Exception("stack overflow!");
            }

            slots[top] = new LuaValue(val);
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
                return slots[absIdx - 1].value;
            }

            return null;
        }

        internal object pop()
        {
            if (top < 1)
            {
                throw new Exception("stack overflow!");
            }

            top--;
            var val = slots[top].value;
            slots[top] = null;
            return val;
        }

        internal void set(int idx, object val)
        {
            var absIdx = absIndex(idx);
            if (absIdx <= 0 || absIdx > top) throw new Exception("invalid index!");
            slots[absIdx - 1] = new LuaValue(val);
        }

        internal void reverse(int from, int to)
        {
            if (to > from)
            {
                System.Array.Reverse(slots, from, to - from + 1);
            }
            else if (to < from)
            {
                System.Array.Reverse(slots, to, from - to + 1);
            }
        }
    }
}