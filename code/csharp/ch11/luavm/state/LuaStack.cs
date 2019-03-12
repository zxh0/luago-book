using System;
using System.Collections.Generic;
using System.Linq;
using luavm.api;

namespace luavm.state
{
    public class LuaStack
    {
        public List<object> slots = new List<object>();
        internal LuaStack prev;
        internal Closure closure;
        internal List<object> varargs;
        internal int pc;
        internal LuaState state;
        internal Dictionary<int, Upvalue> openuvs;

        internal int top()
        {
            return slots.Count();
        }

//        public  LuaStack(int size, LuaState state)
//        {
//            return new LuaStack
//            {
//                slots = new object[size],
//                top = 0,
//                state = state
//            };
//        }

//        internal void check(int n)
//        {
//            var free = slots.Length - top;
//            var slotList = slots.ToList();
//            for (var i = free; i < n; i++)
//            {
//                slotList.Add(null);
//            }
//
//            slots = slotList.ToArray();
//        }

        internal void push(object val)
        {
            if (slots.Count() > slots.Capacity)
            {
                // TODO
                throw new StackOverflowException();
            }

            slots.Add(val);
//            if (top == slots.Length)
//            {
//                throw new Exception("stack overflow!");
//            }
//
//            slots[top] = val;
//
//            top++;
        }

        internal int absIndex(int idx)
        {
            return idx >= 0 || idx <= Consts.LUA_REGISTRYINDEX
                ? idx
                : idx + slots.Count() + 1;
//            
//            if (idx >= 0 || idx < Consts.LUA_REGISTRYINDEX)
//            {
//                return idx;
//            }
//
//            return idx + top + 1;
        }

        internal bool isValid(int idx)
        {
            if (idx < Consts.LUA_REGISTRYINDEX)
            {
                /* upvalues */
                int uvIdx = Consts.LUA_REGISTRYINDEX - idx - 1;
                return closure != null && uvIdx < closure.upvals.Length;
            }

            if (idx == Consts.LUA_REGISTRYINDEX)
            {
                return true;
            }

            var absIdx = absIndex(idx);
            return absIdx > 0 && absIdx <= slots.Count();

//            if (idx < Consts.LUA_REGISTRYINDEX)
//            {
//                var uvIdx = Consts.LUA_REGISTRYINDEX - idx - 1;
//                var c = closure;
//                return c != null && (uvIdx < c.upvals.Length);
//            }
//
//            var absIdx = absIndex(idx);
//            return absIdx > 0 && absIdx <= top;
        }

        internal object get(int idx)
        {
            if (idx < Consts.LUA_REGISTRYINDEX)
            {
                /* upvalues */
                var uvIdx = Consts.LUA_REGISTRYINDEX - idx - 1;
                if (closure != null
                    && closure.upvals.Length > uvIdx
                    && closure.upvals[uvIdx] != null)
                {
                    return closure.upvals[uvIdx].Get();
                }

                return null;
            }

            if (idx == Consts.LUA_REGISTRYINDEX)
            {
                return state.registry;
            }

            var absIdx = absIndex(idx);
            if (absIdx > 0 && absIdx <= slots.Count())
            {
                return slots[absIdx - 1];
            }

            return null;

//            if (idx < Consts.LUA_REGISTRYINDEX)
//            {
//                var uvIdx = Consts.LUA_REGISTRYINDEX - idx - 1;
//                var c = closure;
//                if (c == null || uvIdx >= c.upvals.Length)
//                {
//                    return null;
//                }
//
//                return c.upvals[uvIdx].val;
//            }
//
//            if (idx == Consts.LUA_REGISTRYINDEX)
//            {
//                return state.registry;
//            }
//
//            var absIdx = absIndex(idx);
//            if (absIdx > 0 && absIdx <= top)
//            {
//                return slots[absIdx - 1];
//            }
//
//            return null;
        }

        internal object pop()
        {
            var v = slots.Last();
            slots.RemoveAt(slots.Count - 1);
            return v;
//            if (top < 1)
//            {
//                throw new Exception("stack overflow!");
//            }
//
//            top--;
//            var val = slots[top];
//            slots[top] = null;
//            return val;
        }

        internal void pushN(List<object> vals, int n)
        {
            int nVals = vals?.Count() ?? 0;
            if (n < 0)
            {
                n = nVals;
            }

            for (int i = 0; i < n; i++)
            {
                push(i < nVals ? vals[i] : null);
            }

//            var nVals = vals.Length;
//            if (n < 0)
//            {
//                n = nVals;
//            }
//
//            for (var i = 0; i < n; i++)
//            {
//                if (i < nVals)
//                {
//                    push(vals[i]);
//                }
//                else
//                {
//                    push(null);
//                }
//            }
        }

        internal List<object> popN(int n)
        {
            var vals = new List<object>(n);
            for (int i = 0; i < n; i++)
            {
                vals.Add(pop());
            }

            vals.Reverse();
            return vals;
//            var vals = new object[n];
//            for (var i = n - 1; i >= 0; i--)
//            {
//                vals[i] = pop();
//            }
//
//            return vals;
        }


        internal void set(int idx, object val)
        {
            if (idx < Consts.LUA_REGISTRYINDEX)
            {
                /* upvalues */
                int uvIdx = Consts.LUA_REGISTRYINDEX - idx - 1;
                if (closure != null
                    && closure.upvals.Length > uvIdx
                    && closure.upvals[uvIdx] != null)
                {
                    closure.upvals[uvIdx].Set(val);
                }

                return;
            }

            if (idx == Consts.LUA_REGISTRYINDEX)
            {
                state.registry = (LuaTable) val;
                return;
            }

            int absIdx = absIndex(idx);
            slots.RemoveAt(absIdx - 1);
            slots.Insert(absIdx - 1, val);
        }

        internal void reverse(int from, int to)
        {
            slots.Reverse(from, to - from + 1);
            // Collections.reverse(slots.subList(from, to + 1));
        }
    }
}