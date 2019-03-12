using System;
using luavm.api;
using luavm.number;
using LuaType = System.Int32;
using Math = luavm.number.Math;

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

            switch (value)
            {
                case bool _: return Consts.LUA_TBOOLEAN;
                case double _: return Consts.LUA_TNUMBER;
                case long _: return Consts.LUA_TNUMBER;
                case string _: return Consts.LUA_TSTRING;
                case LuaTable _: return Consts.LUA_TTABLE;
                case Closure _: return Consts.LUA_TFUNCTION;
            }

            Console.WriteLine(value.GetType().Name);
            throw new Exception("todo!");
        }

        internal static (double, bool) convertToFloat(object value)
        {
            switch (value.GetType().Name)
            {
                case "Double": return (toFloat(value), true);
                case "Int64": return (toFloat(value), true);
                case "String": return Parser.ParseFloat(toString(value));
                default: return (0d, false);
            }
        }

        internal static (long, bool) convertToInteger(object val)
        {
            switch (val.GetType().Name)
            {
                case "Int64": return ((long) val, true);
                case "Double": return Math.FloatToInteger((double) val);
                case "String": return (Convert.ToInt64(val), true);
                default: return (0L, false);
            }
        }

        private (long, bool) _stringToInteger(string s)
        {
            var v = Parser.ParseInteger(s);
            if (v.Item2)
            {
                return (v.Item1, true);
            }

            var v2 = Parser.ParseFloat(s);
            if (v2.Item2)
            {
                return Math.FloatToInteger(v2.Item1);
            }

            return (0L, false);
        }


        public static void setMetatable(object val, LuaTable mt, LuaState ls)
        {
            if (val is LuaTable luaTable)
            {
                luaTable.metatable = mt;
                return;
            }

            var key = "_MT" + typeOf(val);
            ls.registry.put(key, mt);
        }

        public static LuaTable getMetatable(object val, LuaState ls)
        {
            if (val is LuaTable luaTable)
            {
                return luaTable.metatable;
            }

            var key = "_MT" + typeOf(val);
            var mt = ls.registry.get(key);
            return mt is LuaTable l ? l : null;
        }
    }
}