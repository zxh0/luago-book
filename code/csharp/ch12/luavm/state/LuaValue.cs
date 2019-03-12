using System;
using luavm.api;
using luavm.number;
using LuaType = System.Int32;
using Math = luavm.number.Math;

namespace luavm.state
{
    public class LuaValue
    {
        internal static LuaType TypeOf(object value)
        {
            if (value is null)
            {
                return Constant.LUA_TNIL;
            }

            switch (value)
            {
                case bool _: return Constant.LUA_TBOOLEAN;
                case double _: return Constant.LUA_TNUMBER;
                case long _: return Constant.LUA_TNUMBER;
                case string _: return Constant.LUA_TSTRING;
                case LuaTable _: return Constant.LUA_TTABLE;
                case Closure _: return Constant.LUA_TFUNCTION;
            }

//            Console.WriteLine(value.GetType().Name);
            throw new Exception("todo!");
        }

        internal static (double, bool) ConvertToFloat(object value)
        {
            switch (value)
            {
                case double d: return (d, true);
                case long l: return (l, true);
                case string s: return Parser.ParseFloat(s);
                default: return (0d, false);
            }
        }

        internal static (long, bool) ConvertToInteger(object val)
        {
            switch (val)
            {
                case long l: return (l, true);
                case double d: return Math.FloatToInteger(d);
                case string _: return (Convert.ToInt64(val), true);
                default: return (0L, false);
            }
        }

        public static void SetMetatable(object val, LuaTable mt, LuaState ls)
        {
            if (val is LuaTable luaTable)
            {
                luaTable.Metatable = mt;
                return;
            }

            var key = "_MT" + TypeOf(val);
            ls.Registry.Put(key, mt);
        }

        public static LuaTable GetMetatable(object val, LuaState ls)
        {
            if (val is LuaTable lt)
            {
                return lt.Metatable;
            }

            var key = "_MT" + TypeOf(val);
            var mt = ls.Registry.Get(key);
            return mt is LuaTable l ? l : null;
        }
    }
}