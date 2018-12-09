using System;
using luavm.binchunk;

namespace luavm.state
{
    public partial struct LuaState : api.LuaState
    {
        public LuaStack stack;

        public static api.LuaState New(int stackSize)
        {
            return new LuaState
            {
                stack = LuaStack.newLuaStack(stackSize)
            };
        }


        internal void pushLuaStack(LuaStack stack)
        {
            stack.prev = this.stack;
            this.stack = stack;
        }

        internal void popLuaStack()
        {
            var stack = this.stack;
            this.stack = stack.prev;
            stack.prev = null;
        }

        public int LuaType { get; set; }

        public int AbsIndex(int idx)
        {
            return stack.absIndex(idx);
        }

        public bool CheckStack(int n)
        {
            stack.check(n);
            return true;
        }

        public int GetTop()
        {
            return stack.top;
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
            var val = stack.pop();
            stack.set(idx, val);
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
            var t = stack.top - 1;
            var p = stack.absIndex(idx) - 1;
            int m;
            if (n >= 0)
            {
                m = t - n;
            }
            else
            {
                m = p - n - 1;
            }

            stack.reverse(p, m);
            stack.reverse(m + 1, t);
            stack.reverse(p, t);
        }

        public void SetTop(int idx)
        {
            var newTop = stack.absIndex(idx);
            if (newTop < 0)
            {
                throw new Exception("stack overflow!");
            }

            var n = stack.top - newTop;
            if (n > 0)
            {
                for (var i = 0; i < n; i++)
                {
                    stack.pop();
                }
            }
            else if (n < 0)
            {
                for (var i = 0; i > n; i--)
                {
                    stack.push(null);
                }
            }
        }
    }
}