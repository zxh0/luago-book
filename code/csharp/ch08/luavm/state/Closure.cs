namespace luavm.state
{
    public struct Closure
    {
        internal binchunk.Prototype proto;

        internal static Closure newLuaClosure(ref binchunk.Prototype proto)
        {
            return new Closure
            {
                proto = proto
            };
        }
    }
}