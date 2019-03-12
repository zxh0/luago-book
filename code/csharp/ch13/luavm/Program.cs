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

                var ls = new state.LuaState();
                ls.Register("print", Print);
                ls.Register("getmetatable", GetMetatable);
                ls.Register("setmetatable", SetMetatable);
                ls.Register("next", Next);
                ls.Register("pairs", Pairs);
                ls.Register("ipairs", IPairs);

                ls.Register("error", Error);
                ls.Register("pcall", PCall);

                ls.Load(ref data, "chunk", "b");
                ls.Call(0, 0);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }

        private static int Print(LuaState ls)
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

        private static int GetMetatable(LuaState ls)
        {
            if (!ls.GetMetatable(1))
            {
                ls.PushNil();
            }

            return 1;
        }

        private static int SetMetatable(LuaState ls)
        {
            ls.SetMetatable(1);
            return 1;
        }

        private static int Next(LuaState ls)
        {
            ls.SetTop(2);
            if (ls.Next(1))
            {
                return 2;
            }
            else
            {
                ls.PushNil();
                return 1;
            }
        }

        private static int Pairs(LuaState ls)
        {
            ls.PushCsharpFunction(Next); /* will return generator, */
            ls.PushValue(1); /* state, */
            ls.PushNil();
            return 3;
        }

        private static int IPairs(LuaState ls)
        {
            ls.PushCsharpFunction(IPairsAux); /* iteration function */
            ls.PushValue(1); /* state */
            ls.PushInteger(0); /* initial value */
            return 3;
        }

        private static int IPairsAux(LuaState ls)
        {
            var i = ls.ToInteger(2) + 1;
            ls.PushInteger(i);
            return ls.GetI(1, i) == Constant.LUA_TNIL ? 1 : 2;
        }

        private static int Error(LuaState ls)
        {
            return ls.Error();
        }

        private static int PCall(LuaState ls)
        {
            var nArgs = ls.GetTop() - 1;
            var status = ls.PCall(nArgs, -1, 0);
            ls.PushBoolean(status == Constant.LUA_OK);
            ls.Insert(1);
            return ls.GetTop();
        }
    }
}