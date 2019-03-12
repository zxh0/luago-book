using System;
using luavm.api;
using CompareOp = System.Int32;

namespace luavm.state
{
    public partial class LuaState
    {
        public bool Compare(int idx1, int idx2, CompareOp op)
        {
            if (!_stack.IsValid(idx1) || !_stack.IsValid(idx2))
            {
                return false;
            }

            var a = _stack.Get(idx1);
            var b = _stack.Get(idx2);
            switch (op)
            {
                case Constant.LUA_OPEQ: return _eq(a, b, this);
                case Constant.LUA_OPLT: return _lt(a, b, this);
                case Constant.LUA_OPLE: return _le(a, b, this);
                default: throw new Exception("invalid compare op!");
            }
        }

        public bool RawEqual(int idx1, int idx2)
        {
            if (!_stack.IsValid(idx1) || !_stack.IsValid(idx2))
            {
                return false;
            }

            var a = _stack.Get(idx1);
            var b = _stack.Get(idx2);
            return _eq(a, b, null);
        }

        private bool _eq(object a, object b, LuaState ls)
        {
            if (a == null)
            {
                return b == null;
            }

            if (b == null)
            {
                return false;
            }

            switch (a)
            {
                case bool aBoolVal:
                    if (b is bool bBoolVal)
                    {
                        return aBoolVal == bBoolVal;
                    }

                    return false;
                case string aStr:
                    if (b is string bStr)
                    {
                        return aStr.Equals(bStr);
                    }

                    return false;
                case long aLong:
                    switch (b)
                    {
                        case long bLong:
                            return aLong == bLong;
                        case double bDouble:
                            return bDouble.Equals(Convert.ToDouble(aLong));
                        default: return false;
                    }
                case double aDouble:
                    switch (b)
                    {
                        case double bDouble: return aDouble.Equals(bDouble);
                        case long bLong: return aDouble.Equals(Convert.ToDouble(bLong));
                        default: return false;
                    }
                case LuaTable aLuaTable:
                    if (!(b is LuaTable bLuaTable) || aLuaTable == bLuaTable || ls == null) return a == b;
                    var (result, ok) = CallMetamethod(aLuaTable, bLuaTable, "__eq", ls);
                    if (ok)
                    {
                        return ConvertToBoolean(result);
                    }

                    return a == b;
                default: return a == b;
            }
        }

        private bool _lt(object a, object b, LuaState ls)
        {
            switch (a)
            {
                case string aStr:
                    if (b is string bStr)
                    {
                        return string.Compare(aStr, bStr, StringComparison.Ordinal) < 0;
                    }

                    break;
                case long aLong:
                    switch (b)
                    {
                        case long bLong: return aLong < bLong;
                        case double bDouble: return aLong < bDouble;
                    }

                    break;
                case double aDouble:
                    switch (b)
                    {
                        case double bDouble: return aDouble < bDouble;
                        case long bLong: return aDouble < bLong;
                    }

                    break;
            }

            var (result, ok) = CallMetamethod(a, b, "__lt", ls);
            if (ok)
            {
                return ConvertToBoolean(result);
            }

            throw new Exception("comparison error!");
        }

        public bool _le(object a, object b, LuaState ls)
        {
            switch (a)
            {
                case string aStr:
                    if (b is string bStr)
                    {
                        return string.CompareOrdinal(aStr, bStr) <= 0;
                    }

                    break;
                case long aLong:
                    switch (b)
                    {
                        case long bLong: return aLong <= bLong;
                        case double bDouble: return Convert.ToDouble(a).CompareTo(bDouble) <= 0;
                    }

                    break;
                case double aDouble:
                    switch (b)
                    {
                        case double bDouble: return aDouble <= bDouble;
                        case long bLong: return aDouble.CompareTo(Convert.ToDouble(bLong)) <= 0;
                    }

                    break;
            }

            var (result, ok) = CallMetamethod(a, b, "__le", ls);
            if (ok)
            {
                return ConvertToBoolean(result);
            }

            var (result2, ok2) = CallMetamethod(b, a, "__lt", ls);
            if (ok2)
            {
                return !ConvertToBoolean(result2);
            }

            throw new Exception("comparison error!");
        }
    }
}