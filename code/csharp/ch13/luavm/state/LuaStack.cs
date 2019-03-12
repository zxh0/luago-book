using System;
using System.Collections;
using System.Collections.Generic;
using luavm.api;

namespace luavm.state
{
    public class LuaStack
    {
        internal readonly ArrayList Slots = new ArrayList();
        internal LuaStack Prev;
        internal Closure Closure;
        internal ArrayList Varargs;
        internal int Pc;
        internal LuaState State;
        internal Dictionary<int, Upvalue> Openuvs;

        internal int Top()
        {
            return Slots.Count;
        }

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

        internal void Push(object val)
        {
            if (Slots.Count > Slots.Capacity)
            {
                // TODO
                throw new StackOverflowException();
            }

            Slots.Add(val);
        }

        internal int AbsIndex(int idx)
        {
            return idx >= 0 || idx <= Constant.LUA_REGISTRYINDEX
                ? idx
                : idx + Slots.Count + 1;
        }

        internal bool IsValid(int idx)
        {
            if (idx < Constant.LUA_REGISTRYINDEX)
            {
                /* upvalues */
                var uvIdx = Constant.LUA_REGISTRYINDEX - idx - 1;
                return Closure != null && uvIdx < Closure.Upvals.Length;
            }

            if (idx == Constant.LUA_REGISTRYINDEX)
            {
                return true;
            }

            var absIdx = AbsIndex(idx);
            return absIdx > 0 && absIdx <= Slots.Count;
        }

        internal object Get(int idx)
        {
            if (idx < Constant.LUA_REGISTRYINDEX)
            {
                /* upvalues */
                var uvIdx = Constant.LUA_REGISTRYINDEX - idx - 1;
                if (Closure != null
                    && Closure.Upvals.Length > uvIdx
                    && Closure.Upvals[uvIdx] != null)
                {
                    return Closure.Upvals[uvIdx].Get();
                }

                return null;
            }

            if (idx == Constant.LUA_REGISTRYINDEX)
            {
                return State.Registry;
            }

            var absIdx = AbsIndex(idx);
            if (absIdx > 0 && absIdx <= Slots.Count)
            {
                return Slots[absIdx - 1];
            }

            return null;
        }

        internal object Pop()
        {
            var v = Slots[Slots.Count - 1];
            Slots.RemoveAt(Slots.Count - 1);
            return v;
        }

        internal void PushN(ArrayList vals, int n)
        {
            var nVals = vals?.Count ?? 0;
            if (n < 0)
            {
                n = nVals;
            }

            for (var i = 0; i < n; i++)
            {
                Push(i < nVals ? vals[i] : null);
            }
        }

        internal ArrayList PopN(int n)
        {
            var vals = new ArrayList(n);
            for (var i = 0; i < n; i++)
            {
                vals.Add(Pop());
            }

            vals.Reverse();
            return vals;
        }


        internal void Set(int idx, object val)
        {
            if (idx < Constant.LUA_REGISTRYINDEX)
            {
                /* upvalues */
                var uvIdx = Constant.LUA_REGISTRYINDEX - idx - 1;
                if (Closure != null
                    && Closure.Upvals.Length > uvIdx
                    && Closure.Upvals[uvIdx] != null)
                {
                    Closure.Upvals[uvIdx].Set(val);
                }

                return;
            }

            if (idx == Constant.LUA_REGISTRYINDEX)
            {
                State.Registry = (LuaTable) val;
                return;
            }

            var absIdx = AbsIndex(idx);
            Slots.RemoveAt(absIdx - 1);
            Slots.Insert(absIdx - 1, val);
        }

        internal void Reverse(int from, int to)
        {
            Slots.Reverse(from, to - from + 1);
            // Collections.reverse(slots.subList(from, to + 1));
        }
    }
}