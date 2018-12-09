using System;
using LuaVm = luavm.api.LuaState;

namespace luavm.vm
{
    public class Instruction
    {
        public Instruction(uint b)
        {
            self = b;
        }

        private uint self;

        public int Opcode()
        {
            return (int) (self & 0x3F);
        }

        public Tuple<int, int, int> ABC()
        {
            return Tuple.Create(
                (int) (self >> 6) & 0xFF,
                (int) (self >> 23) & 0x1FF,
                (int) (self >> 14) & 0x1FF
            );
        }

        public Tuple<int, int> ABx()
        {
            return Tuple.Create((int) ((self >> 6) & 0xFF),
                (int) (self >> 14));
        }

        public const int MAXARG_Bx = (1 << 18) - 1;
        public const int MAXARG_sBx = MAXARG_Bx >> 1;

        public Tuple<int, int> AsBx()
        {
            var tuple = ABx();
            return Tuple.Create(tuple.Item1, tuple.Item2 - MAXARG_sBx);
        }

        public int Ax()
        {
            return (int) (self >> 6);
        }

        public string OpName()
        {
            return OpCodes.opcodes[Opcode()].name;
        }

        public byte OpMode()
        {
            return OpCodes.opcodes[Opcode()].opMode;
        }

        public byte BMode()
        {
            return OpCodes.opcodes[Opcode()].argBMode;
        }

        public byte CMode()
        {
            return OpCodes.opcodes[Opcode()].argCMode;
        }

        public void Execute(LuaVm vm)
        {
            var action = OpCodes.opcodes[Opcode()].action;
            if (action != null)
            {
                action(this, vm);
            }
            else
            {
                throw new Exception(OpName());
            }
        }
    }
}