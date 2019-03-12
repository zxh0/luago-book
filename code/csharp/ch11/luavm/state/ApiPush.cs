using luavm.api;

namespace luavm.state
{
    public partial class LuaState
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
            stack.push(Closure.newCsharpClosure(f, 0));
        }

        public void PushGlobalTable()
        {
            var global = registry.get(Consts.LUA_RIDX_GLOBALS);
            stack.push(global);
        }

        public void PushCsharpClosure(CsharpFunction f, int n)
        {
            var closure = Closure.newCsharpClosure(f, n);
            for (var i = n; i > 0; i--)
            {
                var val = stack.pop();
                closure.upvals[n - 1] = new Upvalue(val);
            }

            stack.push(closure);
        }
    }
}