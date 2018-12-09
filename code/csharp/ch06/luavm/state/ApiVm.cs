namespace luavm.state
{
    public partial struct LuaState
    {
        public int PC()
        {
            return pc;
        }

        public void AddPC(int n)
        {
            pc += n;
        }

        public uint Fetch()
        {
            var i = proto.Code[pc];
            pc++;
            return i;
        }

        public void GetConst(int idx)
        {
            var c = proto.Constants[idx];
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
    }
}