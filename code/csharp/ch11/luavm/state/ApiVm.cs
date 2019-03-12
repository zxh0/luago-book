using System.Collections.Generic;

namespace luavm.state
{
    public partial class LuaState
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
//            Console.WriteLine(stack.pc);
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
            var varargs = stack.varargs ?? new List<object>();
            if (n < 0)
            {
                n = varargs.Count;
            }

            //stack.check(n)
            stack.pushN(varargs, n);
        }

        public void LoadProto(int idx)
        {
            var proto = stack.closure.proto.Protos[idx];
            var closure = Closure.newLuaClosure(proto);
            stack.push(closure);

            for (int i = 0; i < proto.Upvalues.Length; i++)
            {
                var uvInfo = proto.Upvalues[i];
                int uvIdx = uvInfo.Idx;
                if (uvInfo.Instack == 1)
                {
                    if (stack.openuvs == null)
                    {
                        stack.openuvs = new Dictionary<int, Upvalue>();
                    }

                    if (stack.openuvs.ContainsKey(uvIdx))
                    {
                        closure.upvals[i] = stack.openuvs[uvIdx];
                    }
                    else
                    {
                        closure.upvals[i] = new Upvalue(stack, uvIdx);
                        stack.openuvs.Add(uvIdx, closure.upvals[i]);
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
            if (stack.openuvs != null)
            {
                for (var i = 0; i < stack.openuvs.Count; i++)
                {
                    Upvalue uv = stack.openuvs[i];
                    if (uv.Index >= a - 1)
                    {
                        uv.Migrate();
//                        var openuv = stack.openuvs[i];
//                        var val = openuv.val;
//                        openuv.val = val;
                        stack.openuvs.Remove(i);
                    }
                }
            }
        }
    }
}