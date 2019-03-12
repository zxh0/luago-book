using luavm.api;
using luavm.binchunk;

namespace luavm.state
{
    public class Closure
    {
        internal Prototype Proto;
        internal CsharpFunction CsharpFunc;
        internal Upvalue[] Upvals;

        internal static Closure NewLuaClosure(Prototype proto)
        {
            var c = new Closure
            {
                Proto = proto
            };
            if (proto.Upvalues != null && proto.Upvalues.Length > 0)
            {
                c.Upvals = new Upvalue[proto.Upvalues.Length];
            }

            return c;
        }

        internal static Closure NewCsharpClosure(CsharpFunction charpFunc, int nUpvals)
        {
            var c = new Closure
            {
                CsharpFunc = charpFunc
            };
            if (nUpvals > 0)
            {
                c.Upvals = new Upvalue[nUpvals];
            }

            return c;
        }
    }
}