using luavm.api;

namespace luavm.state
{
    public partial class LuaState
    {
        public void PushNil()
        {
            _stack.Push(null);
        }

        public void PushBoolean(bool b)
        {
            _stack.Push(b);
        }

        public void PushInteger(long n)
        {
            _stack.Push(n);
        }

        public void PushNumber(double n)
        {
            _stack.Push(n);
        }

        public void PushString(string s)
        {
            _stack.Push(s);
        }

        public void PushCsharpFunction(CsharpFunction f)
        {
            _stack.Push(Closure.NewCsharpClosure(f, 0));
        }

        public void PushGlobalTable()
        {
            var global = Registry.Get(Constant.LUA_RIDX_GLOBALS);
            _stack.Push(global);
        }

        public void PushCsharpClosure(CsharpFunction f, int n)
        {
            var closure = Closure.NewCsharpClosure(f, n);
            for (var i = n; i > 0; i--)
            {
                var val = _stack.Pop();
                closure.Upvals[n - 1] = new Upvalue(val);
            }

            _stack.Push(closure);
        }
    }
}