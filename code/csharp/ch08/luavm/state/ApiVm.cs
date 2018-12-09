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
            var proto = stack.closure.proto.Protos[idx];
            var closure = Closure.newLuaClosure(ref proto);
            stack.push(closure);
        }
    }
}