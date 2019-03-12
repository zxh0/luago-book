using luavm.api;

namespace luavm.state
{
    public struct Closure
    {
        internal binchunk.Prototype proto;
        internal CsharpFunction csharpFunc;

        internal static Closure newLuaClosure(binchunk.Prototype proto)
        {
            return new Closure
            {
                proto = proto
            };
        }

        internal static Closure newCsharpClosure(CsharpFunction charpFunc)
        {
            return new Closure
            {
                csharpFunc = charpFunc
            };
        }
    }
}