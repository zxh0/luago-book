using System;
using luavm.api;

namespace luavm.state
{
    public partial class LuaState : api.LuaState, LuaVM
    {
        public LuaTable registry = new LuaTable(0, 0);
        private LuaStack stack = new LuaStack();
 
        public LuaState()
        {
            registry.put(Consts.LUA_RIDX_GLOBALS, new LuaTable(0, 0));
            LuaStack stack = new LuaStack();
            stack.state = this;
            pushLuaStack(stack);
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

        private void pushLuaStack(LuaStack stack)
        {
            stack.prev = this.stack;
            this.stack = stack;
        }

        private void popLuaStack()
        {
            var top = stack;
            stack = top.prev;
            top.prev = null;
        }

       // public int LuaType { get; set; }

        public int AbsIndex(int idx)
        {
            return stack.absIndex(idx);
        }

        public bool CheckStack(int n)
        {
//            stack.check(n);
            return true;
        }

        public int GetTop()
        {
            return stack.top();
        }

        public void Pop(int n)
        {
            SetTop(-n - 1);
        }

        public void Copy(int fromIdx, int toIdx)
        {
            var val = stack.get(fromIdx);
            stack.set(toIdx, val);
        }

        public void PushValue(int idx)
        {
            var val = stack.get(idx);
            stack.push(val);
        }

        public void Replace(int idx)
        {
            stack.set(idx, stack.pop());
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
            var t = stack.top() - 1; /* end of stack segment being rotated */
            var p = stack.absIndex(idx) - 1; /* start of segment */
            var m = n >= 0 ? t - n : p - n - 1; /* end of prefix */

            stack.reverse(p, m); /* reverse the prefix with length 'n' */
            stack.reverse(m + 1, t); /* reverse the suffix */
            stack.reverse(p, t);
        }

        public void SetTop(int idx)
        {
            int newTop = stack.absIndex(idx);
            if (newTop < 0)
            {
                throw new Exception("stack underflow!");
            }

            int n = stack.top() - newTop;
            if (n > 0)
            {
                for (int i = 0; i < n; i++)
                {
                    stack.pop();
                }
            }
            else if (n < 0)
            {
                for (int i = 0; i > n; i--)
                {
                    stack.push(null);
                }
            }
        }

        internal static int LuaUpvalueIndex(int i)
        {
            return Consts.LUA_REGISTRYINDEX - i;
        }
    }
}