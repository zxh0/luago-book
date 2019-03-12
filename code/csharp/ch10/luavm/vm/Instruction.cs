using System;
using luavm.api;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public class Instruction
    {
        public Instruction(uint b)
        {
            self = b;
        }

        private readonly uint self;

        public int Opcode()
        {
            return (int) (self & 0x3F);
        }

        public (int, int, int) ABC()
        {
            return (
                (int) (self >> 6) & 0xFF,
                (int) (self >> 23) & 0x1FF,
                (int) (self >> 14) & 0x1FF
            );
        }

        public (int, int) ABx()
        {
            return ((int) ((self >> 6) & 0xFF),
                (int) (self >> 14));
        }

        private const int MAXARG_Bx = (1 << 18) - 1;
        private const int MAXARG_sBx = MAXARG_Bx >> 1;

        public (int, int) AsBx()
        {
            var tuple = ABx();
            return (tuple.Item1, tuple.Item2 - MAXARG_sBx);
        }

        public int Ax()
        {
            return (int) (self >> 6);
        }

        private string OpName()
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

        private static void printStack(LuaState ls)
        {
            var top = ls.GetTop();
            for (var i = 1; i <= top; i++)
            {
                var t = ls.Type(i);
                switch (t)
                {
                    case Consts.LUA_TBOOLEAN:
                        Console.Write($"[{ls.ToBoolean(i)}]");
                        break;
                    case Consts.LUA_TNUMBER:
                        Console.Write($"[{ls.ToNumber(i)}]");
                        break;
                    case Consts.LUA_TSTRING:
                        Console.Write($"[{ls.ToString(i)}]");
                        break;
                    default:
                        Console.Write($"[{ls.TypeName(t)}]");
                        break;
                }
            }

            Console.WriteLine();
        }

        public void Execute(ref LuaVM vm)
        {
            var action = OpCodes.opcodes[Opcode()].action;

            //Console.Write(Opcode());
            //printStack(vm);

//            var ints = new int[] {11, 45, 43, 4};
//            foreach (var i in ints)
//            {
//                if (Opcode() == i)
//                {
//                    Console.Write(Opcode());
//                    printStack(vm);
//                }
//            }

            if (action != null)
            {
                action(this, ref vm);
            }
            else
            {
                throw new Exception(OpName());
            }
        }
    }
}