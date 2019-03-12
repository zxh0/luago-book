using System;
using System.IO;
using luavm.api;

namespace luavm
{
    internal static class Program
    {
        private static void Main(string[] args)
        {
//            var l=new List<int>();
//            l.Add(1);
//            l.Add(2);
//            l.Add(3);
//                l.Add(4);
//            l.Reverse(1,2);
//            l.ForEach(Console.WriteLine);

            if (args.Length <= 0) return;
            try
            {
                var fs = File.OpenRead(args[0]);
                var data = new byte[fs.Length];
                fs.Read(data, 0, data.Length);

                var ls =new state.LuaState();
                ls.Register("print", print);
                ls.Register("getmetatable", getMetatable);
                ls.Register("setmetatable", setMetatable);
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

        private static int getMetatable(LuaState ls)
        {
            if (!ls.GetMetatable(1))
            {
                ls.PushNil();
            }

            return 1;
        }

        private static int setMetatable(LuaState ls)
        {
            ls.SetMetatable(1);
            return 1;
        }
    }
}