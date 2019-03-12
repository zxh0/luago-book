using System.Collections.Generic;

namespace luavm.state
{
    public partial struct LuaState
    {
        public int PC()
        {
            return stack.pc;
        }

        public void AddPC(int n)
        {
            stack.pc += n;
        }

        public uint Fetch()
        {
            var i = stack.closure.proto.Code[stack.pc];
            stack.pc++;
            return i;
        }

        public void GetConst(int idx)
        {
            var c = stack.closure.proto.Constants[idx];
            stack.push(c);
        }

        public void GetRK(int rk)
        {
            if (rk > 0xFF)
            {
                GetConst(rk & 0xFF);
            }
            else
            {
                PushValue(rk + 1);
            }
        }

        public int RegisterCount()
        {
            return stack.closure.proto.MaxStackSize;
        }

        public void LoadVararg(int n)
        {
            if (n < 0)
            {
                n = stack.varargs.Length;
            }

            stack.check(n);
            stack.pushN(stack.varargs, n);
        }

        public void LoadProto(int idx)
        {
            var subProto = stack.closure.proto.Protos[idx];
            var closure = Closure.newLuaClosure(subProto);
            stack.push(closure);
            for (var i = 0; i < subProto.Upvalues.Length; i++)
            {
                var uvInfo = subProto.Upvalues[i];
                var uvIdx = (int) uvInfo.Idx;
                if (uvInfo.Instack == 1)
                {
                    if (stack.openuvs == null)
                    {
                        stack.openuvs = new Dictionary<int, Upvalue>();
                    }

                    if (stack.openuvs.ContainsKey(uvIdx))
                    {
                        var openuv = stack.openuvs[uvIdx];
                        closure.upvals[i] = openuv;
                    }
                    else
                    {
                        closure.upvals[i] = new Upvalue {val = stack.slots[uvIdx]};
                        stack.openuvs[uvIdx] = closure.upvals[i];
                    }
                }
                else
                {
                    closure.upvals[i] = stack.closure.upvals[uvIdx];
                }
            }
        }

        public void CloseUpvalues(int a)
        {
            for (var i = 0; i < stack.openuvs.Count; i++)
            {
              if (i >= a - 1)
                {  var openuv = stack.openuvs[i];
                    var val= openuv.val;
                    openuv.val = val;
                    stack.openuvs.Remove(i);
                }
            }
        }
    }
}