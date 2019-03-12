using System;
using System.Collections.Generic;
using luavm.api;
using LuaType = System.Int32;

namespace luavm.state
{
    public class LuaValue
    {
//        internal readonly object value;

//        public LuaValue(object value)
//        {
//            this.value = value;
//        }

        public static string toString(object value)
        {
            return Convert.ToString(value);
        }

//        public LuaValue toLuaValue()
//        {
//            return (LuaValue) (value);
//        }

        public static long toInteger(object value)
        {
            return Convert.ToInt64(value);
        }


        public static double toFloat(object value)
        {
            return Convert.ToDouble(value);
        }

        public static bool isString(object value)
        {
            return value.GetType().IsEquivalentTo(typeof(string));
        }

        public static bool isLuaValue(object value)
        {
            return value.GetType().IsEquivalentTo(typeof(LuaValue));
        }

        public static LuaTable toLuaTable(object value)
        {
            return (LuaTable) value;
        }

        public static bool isLuaTable(object value)
        {
            return value.GetType().IsEquivalentTo(typeof(LuaTable));
        }

        public static bool isFloat(object value)
        {
            return value.GetType().IsEquivalentTo(typeof(double));
        }

        public static bool isInteger(object value)
        {
            return value.GetType().IsEquivalentTo(typeof(long));
        }

        internal static LuaType typeOf(object value)
        {
            if (value == null)
            {
                return Consts.LUA_TNIL;
            }

            // Console.WriteLine("\n" + value.GetType().Name);

            switch (value.GetType().Name)
            {
                case "Boolean": return Consts.LUA_TBOOLEAN;
                case "Double": return Consts.LUA_TNUMBER;
                case "Int64": return Consts.LUA_TNUMBER;
                case "String": return Consts.LUA_TSTRING;
                case "LuaTable": return Consts.LUA_TTABLE;
                case "Closure": return Consts.LUA_TFUNCTION;
            }

            throw new Exception("todo!");
        }

        internal static (double, bool) convertToFloat(object value)
        {
            switch (value.GetType().Name)
            {
                case "Double": return (toFloat(value), true);
                case "Int64": return (toFloat(value), true);
                case "String": return number.Parser.ParseFloat(toString(value));
                default: return (0d, false);
            }
        }

        internal static (long, bool) convertToInteger(object val)
        {
            switch (val.GetType().Name)
            {
                case "Int64": return ((long) val, true);
                case "Double": return number.Math.FloatToInteger((double) val);
                case "String": return (Convert.ToInt64(val), true);
                default: return (0L, false);
            }
        }

        private (long, bool) _stringToInteger(string s)
        {
            var v = number.Parser.ParseInteger(s);
            if (v.Item2)
            {
                return (v.Item1, true);
            }

            var v2 = number.Parser.ParseFloat(s);
            if (v2.Item2)
            {
                return number.Math.FloatToInteger(v2.Item1);
            }

            return (0L, false);
        }
    }
}