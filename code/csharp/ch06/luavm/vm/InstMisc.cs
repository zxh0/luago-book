using System;
using LuaVm = luavm.api.LuaState;

namespace luavm.vm
{
    public class InstMisc
    {
        internal static void move(Instruction i, LuaVm vm)
        {
            var ab_ = i.ABC();
            var a = ab_.Item1 + 1;
            var b = ab_.Item2;
            vm.Copy(b, a);
        }

        internal static void jmp(Instruction i, LuaVm vm)
        {
            var asBx = i.AsBx();
            var a = asBx.Item1;
            var sBx = asBx.Item2;
            
            vm.AddPC(sBx);
            if (a != 0)
            {
                throw new Exception("todo!");
            }
        }
    }
}