using System;
using luavm.api;

namespace luavm.state
{
    public partial class LuaState : api.LuaState, LuaVM
    {
        public LuaTable Registry = new LuaTable(0, 0);
        private LuaStack _stack = new LuaStack();
 
        public LuaState()
        {
            Registry.Put(Constant.LUA_RIDX_GLOBALS, new LuaTable(0, 0));
            var stack = new LuaStack {State = this};
            PushLuaStack(stack);
//            var registry = LuaTable.newLuaTable(0, 0);
//            registry.put(Consts.LUA_RIDX_GLOBALS, LuaTable.newLuaTable(0, 0));
//
//            var ls = new LuaState
//            {
//                registry = registry
//            };
//            ls.pushLuaStack(LuaStack.newLuaStack(Consts.LUA_MINSTACK, ls));
//            return ls;
        }

        private void PushLuaStack(LuaStack stack)
        {
            stack.Prev = this._stack;
            this._stack = stack;
        }

        private void PopLuaStack()
        {
            var top = _stack;
            _stack = top.Prev;
            top.Prev = null;
        }

       // public int LuaType { get; set; }

        public int AbsIndex(int idx)
        {
            return _stack.AbsIndex(idx);
        }

        public bool CheckStack(int n)
        {
//            stack.check(n);
            return true;
        }

        public int GetTop()
        {
            return _stack.Top();
        }

        public void Pop(int n)
        {
            for (var i = 0; i < n; i++) {
                _stack.Pop();
            }
        }

        public void Copy(int fromIdx, int toIdx)
        {
            var val = _stack.Get(fromIdx);
            _stack.Set(toIdx, val);
        }

        public void PushValue(int idx)
        {
            var val = _stack.Get(idx);
            _stack.Push(val);
        }

        public void Replace(int idx)
        {
            _stack.Set(idx, _stack.Pop());
        }

        public void Insert(int idx)
        {
            Rotate(idx, 1);
        }

        public void Remove(int idx)
        {
            Rotate(idx, -1);
            Pop(1);
        }

        public void Rotate(int idx, int n)
        {
            var t = _stack.Top() - 1; /* end of stack segment being rotated */
            var p = _stack.AbsIndex(idx) - 1; /* start of segment */
            var m = n >= 0 ? t - n : p - n - 1; /* end of prefix */

            _stack.Reverse(p, m); /* reverse the prefix with length 'n' */
            _stack.Reverse(m + 1, t); /* reverse the suffix */
            _stack.Reverse(p, t);
        }

        public void SetTop(int idx)
        {
            var newTop = _stack.AbsIndex(idx);
            if (newTop < 0)
            {
                throw new Exception("stack underflow!");
            }

            var n = _stack.Top() - newTop;
            if (n > 0)
            {
                _stack.Slots.RemoveRange( _stack.Slots.Count-n,n);
            }
            else if (n < 0)
            {
                for (var i = 0; i > n; i--)
                {
                    _stack.Slots.Add(null);
                }
            }
        }

        internal static int LuaUpvalueIndex(int i)
        {
            return Constant.LUA_REGISTRYINDEX - i;
        }
    }
}