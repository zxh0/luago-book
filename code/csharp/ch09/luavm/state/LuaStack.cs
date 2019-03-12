using System;
using System.Linq;
using System.Reflection;
using System.Threading;
using luavm.api;

namespace luavm.state
{
    public class LuaStack
    {
        public object[] slots;
        internal int top;
        internal LuaStack prev;
        internal Closure closure;
        internal object[] varargs;
        internal int pc;
        internal LuaState state;

        internal static LuaStack newLuaStack(int size, LuaState state)
        {
            return new LuaStack
            {
                slots = new object[size],
                top = 0,
                state = state
            };
        }

        internal void check(int n)
        {
            var free = slots.Length - top;
            var slotList = slots.ToList();
            for (var i = free; i < n; i++)
            {
                slotList.Add(null);
            }

            slots = slotList.ToArray();
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
            if (idx >= 0 || idx < Consts.LUA_REGISTRYINDEX)
            {
                return idx;
            }

            return idx + top + 1;
        }

        internal bool isValid(int idx)
        {
            if (idx == Consts.LUA_REGISTRYINDEX)
            {
                return true;
            }

            var absIdx = absIndex(idx);
            return absIdx > 0 && absIdx <= top;
        }

        internal object get(int idx)
        {
            if (idx == Consts.LUA_REGISTRYINDEX)
            {
                return state.registry;
            }

            var absIdx = absIndex(idx);
            if (absIdx > 0 && absIdx <= top)
            {
                return slots[absIdx - 1];
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
            var val = slots[top];
            slots[top] = null;
            return val;
        }

        internal void pushN(object[] vals, int n)
        {
            var nVals = vals.Length;
            if (n < 0)
            {
                n = nVals;
            }

            for (var i = 0; i < n; i++)
            {
                if (i < nVals)
                {
                    push(vals[i]);
                }
                else
                {
                    push(null);
                }
            }
        }

        internal object[] popN(int n)
        {
            var vals = new object[n];
            for (var i = n - 1; i >= 0; i--)
            {
                vals[i] = pop();
            }

            return vals;
        }


        internal void set(int idx, object val)
        {
            if (idx == Consts.LUA_REGISTRYINDEX)
            {
                state.registry = (LuaTable) val;
                return;
            }

            var absIdx = absIndex(idx);
            if (absIdx <= 0 || absIdx > top)
            {
                throw new Exception("invalid index!");
            }

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