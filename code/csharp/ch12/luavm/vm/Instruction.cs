using System;
using luavm.api;
using LuaVM = luavm.state.LuaState;

namespace luavm.vm
{
    public class Instruction
    {
        public Instruction(uint b)
        {
            _self = b;
        }

        private readonly uint _self;

        public int Opcode()
        {
            return (int) (_self & 0x3F);
        }

        public (int, int, int) ABC()
        {
            return (
                (int) (_self >> 6) & 0xFF,
                (int) (_self >> 23) & 0x1FF,
                (int) (_self >> 14) & 0x1FF
            );
        }

        public (int, int) ABx()
        {
            return ((int) ((_self >> 6) & 0xFF),
                (int) (_self >> 14));
        }

        private const int MAXARG_Bx = (1 << 18) - 1;
        private const int MAXARG_sBx = MAXARG_Bx >> 1;

        public (int, int) AsBx()
        {
            var (a, sBx) = ABx();
            return (a, sBx - MAXARG_sBx);
        }

        public int Ax()
        {
            return (int) (_self >> 6);
        }

        private string OpName()
        {
            return Constant.Opcodes[Opcode()].Name;
        }

        public byte OpMode()
        {
            return Constant.Opcodes[Opcode()].OpMode;
        }

        public byte BMode()
        {
            return Constant.Opcodes[Opcode()].ArgBMode;
        }

        public byte CMode()
        {
            return Constant.Opcodes[Opcode()].ArgCMode;
        }

        private static void printStack(LuaState ls)
        {
            var top = ls.GetTop();
            for (var i = 1; i <= top; i++)
            {
                var t = ls.Type(i);
                switch (t)
                {
                    case Constant.LUA_TBOOLEAN:
                        Console.Write($"[{ls.ToBoolean(i)}]");
                        break;
                    case Constant.LUA_TNUMBER:
                        Console.Write($"[{ls.ToNumber(i)}]");
                        break;
                    case Constant.LUA_TSTRING:
                        Console.Write($"[{ls.ToString(i)}]");
                        break;
                    default:
                        Console.Write($"[{ls.TypeName(t)}]");
                        break;
                }
            }

            Console.WriteLine();
        }

        public void Execute(Opcode op, LuaVM vm)
        {
            var action = op.Action;

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