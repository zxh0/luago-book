using System;
using System.IO;
using luavm.api;
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

                var ls = LuaVM.New();
                ls.Register("print", print);
                ls.Load(ref data, "chunk", "b");
                ls.Call(0, 0);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }

            //            Console.ReadKey();
        }

        static int print(LuaState ls)
        {
            var nArgs = ls.GetTop();
            for (var i = 1; i <= nArgs; i++)
            {
                if (ls.IsBoolean(i))
                {
                    Console.Write("{0}", ls.ToBoolean(i));
                }
                else if (ls.IsString(i))
                {
                    Console.Write(ls.ToString(i));
                }
                else
                {
                    Console.Write(ls.TypeName(ls.Type(i)));
                }

                if (i < nArgs)
                {
                    Console.Write("\t");
                }
            }

            Console.WriteLine();
            return 0;
        }
    }
}