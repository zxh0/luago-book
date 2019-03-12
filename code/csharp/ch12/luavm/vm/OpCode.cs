using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public delegate void Action(Instruction i, ref LuaVM vm);

    public struct Opcode
    {
        private byte _testFlag;
        private byte _setAFlag;
        internal readonly byte ArgBMode;
        internal readonly byte ArgCMode;
        internal readonly byte OpMode;
        internal readonly string Name;
        internal readonly Action Action;

        public Opcode(byte testFlag, byte aFlag, byte argBMode, byte argCMode, byte opMode, string name, Action action)
        {
            _testFlag = testFlag;
            _setAFlag = aFlag;
            ArgBMode = argBMode;
            ArgCMode = argCMode;
            OpMode = opMode;
            Name = name;
            Action = action;
        }
    }
}