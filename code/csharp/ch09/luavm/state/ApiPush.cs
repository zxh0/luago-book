using luavm.api;

namespace luavm.state
{
    public partial struct LuaState
    {
        public void PushNil()
        {
            stack.push(null);
        }

        public void PushBoolean(bool b)
        {
            stack.push(b);
        }

        public void PushInteger(long n)
        {
            stack.push(n);
        }

        public void PushNumber(double n)
        {
            stack.push(n);
        }

        public void PushString(string s)
        {
            stack.push(s);
        }

        public void PushCsharpFunction(CsharpFunction f)
        {
            stack.push(Closure.newCsharpClosure(f));
        }

        public void PushGlobalTable()
        {
            var global = registry.get(Consts.LUA_RIDX_GLOBALS);
            stack.push(global);
        }
    }
}