namespace luavm.state
{
    public class Upvalue
    {
        internal readonly int Index;
        private LuaStack _stack;
        private object _val;

        public Upvalue(object value)
        {
            _val = value;
            Index = 0;
        }

        public Upvalue(LuaStack stack, int index)
        {
            _stack = stack;
            Index = index;
        }

        public object Get()
        {
            return _stack != null ? _stack.Get(Index + 1) : _val;
        }

        public void Set(object val)
        {
            if (_stack != null)
            {
                _stack.Set(Index + 1, val);
            }
            else
            {
                _val = val;
            }
        }

        public void Migrate()
        {
            if (_stack == null) return;
            _val = _stack.Get(Index + 1);
            _stack = null;
        }
    }
}