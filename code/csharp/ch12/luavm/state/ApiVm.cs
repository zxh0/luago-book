using System.Collections;
using System.Collections.Generic;

namespace luavm.state
{
    public partial class LuaState
    {
        public int PC()
        {
            return _stack.Pc;
        }

        public void AddPC(int n)
        {
            _stack.Pc += n;
        }

        public uint Fetch()
        {
            var i = _stack.Closure.Proto.Code[_stack.Pc++];
            return i;
        }

        public void GetConst(int idx)
        {
            var c = _stack.Closure.Proto.Constants[idx];
            _stack.Push(c);
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
            return _stack.Closure.Proto.MaxStackSize;
        }

        public void LoadVararg(int n)
        {
            var varargs = _stack.Varargs ?? new ArrayList();
            if (n < 0)
            {
                n = varargs.Count;
            }

            //stack.check(n)
            _stack.PushN(varargs, n);
        }

        public void LoadProto(int idx)
        {
            var proto = _stack.Closure.Proto.Protos[idx];
            var closure = Closure.NewLuaClosure(proto);
            _stack.Push(closure);

            for (var i = 0; i < proto.Upvalues.Length; i++)
            {
                var uvInfo = proto.Upvalues[i];
                int uvIdx = uvInfo.Idx;
                if (uvInfo.Instack == 1)
                {
                    if (_stack.Openuvs == null)
                    {
                        _stack.Openuvs = new Dictionary<int, Upvalue>();
                    }

                    if (_stack.Openuvs.ContainsKey(uvIdx))
                    {
                        closure.Upvals[i] = _stack.Openuvs[uvIdx];
                    }
                    else
                    {
                        closure.Upvals[i] = new Upvalue(_stack, uvIdx);
                        _stack.Openuvs.Add(uvIdx, closure.Upvals[i]);
                    }
                }
                else
                {
                    closure.Upvals[i] = _stack.Closure.Upvals[uvIdx];
                }
            }
        }

        public void CloseUpvalues(int a)
        {
            if (_stack.Openuvs == null) return;
            for (var i = 0; i < _stack.Openuvs.Count; i++)
            {
                var uv = _stack.Openuvs[i];
                if (uv.Index < a - 1) continue;
                uv.Migrate();
                _stack.Openuvs.Remove(i);
            }
        }
    }
}