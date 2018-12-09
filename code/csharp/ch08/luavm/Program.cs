using System;
using System.IO;
using luavm.api;
using luavm.binchunk;
using luavm.vm;
using LuaVM = luavm.state.LuaState;

namespace luavm
{
    internal static class Program
    {
        private static void Main(string[] args)
        {
            if (args.Length <= 0) return;
            try
            {
                var fs = File.OpenRead(args[0]);
                var data = new byte[fs.Length];
                fs.Read(data, 0, data.Length);

                var ls = state.LuaState.New(20);
                ls.Load(ref data, args[0], "b");
                ls.Call(0, 0);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }

            //            Console.ReadKey();
        }
    }
}