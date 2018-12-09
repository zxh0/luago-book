using System;
using System.Collections.Generic;
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

        public LuaTable toLuaTable()
        {
            return (LuaTable) value;
        }

        public string toString()
        {
            return Convert.ToString(value);
        }

        public long toInteger()
        {
            return Convert.ToInt64(value);
        }

        public double toFloat()
        {
            return Convert.ToDouble(value);
        }

        public bool isString()
        {
            return value.GetType().IsEquivalentTo(typeof(string));
        }

        public bool isLuaTable()
        {
            return value.GetType().IsEquivalentTo(typeof(LuaTable));
        }

        public bool isFloat()
        {
            return value.GetType().IsEquivalentTo(typeof(double));
        }

        public bool isInteger()
        {
            return value.GetType().IsEquivalentTo(typeof(long));
        }

        internal LuaType typeOf()
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
               }

            throw new Exception("todo!");
        }

        internal static Tuple<double, bool> convertToFloat(LuaValue luaValue)
        {
            switch (luaValue.value.GetType().Name)
            {
                case "Double": return Tuple.Create(luaValue.toFloat(), true);
                case "Int64": return Tuple.Create(luaValue.toFloat(), true);
                case "String": return number.Parser.ParseFloat(Convert.ToString(luaValue.value));
                default: return Tuple.Create(0d, false);
            }
        }

        internal static Tuple<long, bool> convertToInteger(object val)
        {
            switch (val.GetType().Name)
            {
                case "Int64": return Tuple.Create((long) val, true);
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

            return Tuple.Create(0L, false);
        }
    }
}