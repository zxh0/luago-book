using System;
using luavm.api;
using LuaType = System.Int32;

namespace luavm.state
{
    public class LuaValue
    {
        internal readonly object value;

        public LuaValue(object value)
        {
            this.value = value;
        }

        internal static LuaType typeOf(object val)
        {
            if (val == null)
            {
                return Consts.LUA_TNIL;
            }

            //Console.WriteLine(val.GetType().Name);
            switch (val.GetType().Name)
            {
                case "Boolean": return Consts.LUA_TBOOLEAN;
                case "Double": return Consts.LUA_TNUMBER;
                case "Int64": return Consts.LUA_TNUMBER;
                case "String": return Consts.LUA_TSTRING;
                default: throw new Exception("todo!");
            }
        }

        internal static Tuple<double, bool> convertToFloat(object val)
        {
            switch (val.GetType().Name)
            {
                case "Double": return Tuple.Create((double) val, true);
                case "Int64": return Tuple.Create(Convert.ToDouble(val), true);
                case "String": return number.Parser.ParseFloat((string) val);
                default: return Tuple.Create(0d, false);
            }
        }

        internal static Tuple<long, bool> convertToInteger(object val)
        {
            switch (val.GetType().Name)
            {
                case "Int64": return Tuple.Create<long, bool>((long) val, true);
                case "Double": return number.Math.FloatToInteger((double) val);
                case "String": return Tuple.Create(Convert.ToInt64(val), true);
                default: return Tuple.Create(0L, false);
            }
        }

        private Tuple<long, bool> _stringToInteger(string s)
        {
            var v = number.Parser.ParseInteger(s);
            if (v.Item2)
            {
                return Tuple.Create(v.Item1, true);
            }

            var v2 = number.Parser.ParseFloat(s);
            if (v2.Item2)
            {
                return number.Math.FloatToInteger(v2.Item1);
            }

            return Tuple.Create<long, bool>(0L, false);
        }
    }
}