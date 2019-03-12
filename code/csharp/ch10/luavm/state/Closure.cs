using luavm.api;
using luavm.binchunk;

namespace luavm.state
{
    public struct Upvalue
    {
        internal object val;
    }

    public class Closure
    {
        internal Prototype proto;
        internal CsharpFunction csharpFunc;
        internal Upvalue[] upvals;

        internal static Closure newLuaClosure(Prototype proto)
        {
            var c = new Closure
            {
                proto = proto
            };
            if (proto.Upvalues != null && proto.Upvalues.Length > 0)
            {
                c.upvals = new Upvalue[proto.Upvalues.Length];
            }

            return c;
        }

        internal static Closure newCsharpClosure(CsharpFunction charpFunc, int nUpvals)
        {
            var c = new Closure
            {
                csharpFunc = charpFunc
            };
            if (nUpvals > 0)
            {
                c.upvals = new Upvalue[nUpvals];
            }

            return c;
        }
    }
}