using System;
using luavm.api;
using LuaType = System.Int32;

namespace luavm.state
{
    public partial class LuaState
    {
        public string TypeName(LuaType tp)
        {
            switch (tp)
            {
                case Constant.LUA_TNONE: return "no value";
                case Constant.LUA_TNIL: return "nil";
                case Constant.LUA_TBOOLEAN: return "boolean";
                case Constant.LUA_TNUMBER: return "number";
                case Constant.LUA_TSTRING: return "string";
                case Constant.LUA_TTABLE: return "table";
                case Constant.LUA_TFUNCTION: return "function";
                case Constant.LUA_TTHREAD: return "thread";
                default: return "userdata";
            }
        }

        public LuaType Type(int idx)
        {
            if (!_stack.IsValid(idx))
            {
                return Constant.LUA_TNONE;
            }

            var val = _stack.Get(idx);
            return LuaValue.TypeOf(val);
        }

        public bool IsNone(int idx)
        {
            return Type(idx) == Constant.LUA_TNONE;
        }

        public bool IsNil(int idx)
        {
            return Type(idx) == Constant.LUA_TNIL;
        }

        public bool IsNoneOrNil(int idx)
        {
            return Type(idx) <= Constant.LUA_TNIL;
        }

        public bool IsBoolean(int idx)
        {
            return Type(idx) == Constant.LUA_TBOOLEAN;
        }

        public bool IsString(int idx)
        {
            var t = Type(idx);
            return t == Constant.LUA_TSTRING || t == Constant.LUA_TNUMBER;
        }

        public bool IsNumber(int idx)
        {
            return ToNumberX(idx).Item2;
        }

        public bool IsInteger(int idx)
        {
            var val = _stack.Get(idx);
            return val is long;
        }

        public bool IsCsharpFunction(int idx)
        {
            var val = _stack.Get(idx);
            if (val is Closure c)
            {
                return c.CsharpFunc != null;
            }

            return false;
        }

        public bool ToBoolean(int idx)
        {
            var val = _stack.Get(idx);
            return ConvertToBoolean(val);
        }


        private static bool ConvertToBoolean(object val)
        {
            if (val == null)
            {
                return false;
            }

            switch (val)
            {
                case bool b: return b;
                default: return true;
            }
        }

        public double ToNumber(int idx)
        {
            return ToNumberX(idx).Item1;
        }

        public (double, bool) ToNumberX(int idx)
        {
            var val = _stack.Get(idx);
            return LuaValue.ConvertToFloat(val);
        }

        public (long, bool) ToIntegerX(int idx)
        {
            var val = _stack.Get(idx);
            return LuaValue.ConvertToInteger(val);
        }

        public long ToInteger(int idx)
        {
            var val = ToIntegerX(idx);
            return val.Item1;
        }

        public string ToString(int idx)
        {
            return ToStringX(idx).Item1;
        }

        public (string, bool) ToStringX(int idx)
        {
            var val = _stack.Get(idx);
            switch (val)
            {
                case string valStr: return (valStr, true);
                case long _:
                case double _:
                    var s = val;
                    _stack.Set(idx, s);
                    return (Convert.ToString(s), true);
                default: return (null, false);
            }
        }

        public CsharpFunction ToCsharpFunction(int idx)
        {
            var val = _stack.Get(idx);
            if (val is Closure c)
            {
                return c.CsharpFunc;
            }

            return null;
        }
    }
}