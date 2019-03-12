using System;
using luavm.api;
using CompareOp = System.Int32;

namespace luavm.state
{
    public partial struct LuaState
    {
        public bool Compare(int idx1, int idx2, CompareOp op)
        {
            if (!stack.isValid(idx1) || !stack.isValid(idx2))
            {
                return false;
            }

            var a = stack.get(idx1);
            var b = stack.get(idx2);
            switch (op)
            {
                case Consts.LUA_OPEQ: return _eq(a, b);
                case Consts.LUA_OPLT: return _lt(a, b);
                case Consts.LUA_OPLE: return _le(a, b);
                default: throw new Exception("invalid compare op!");
            }
        }

        bool _eq(object a, object b)
        {
            if (a == null)
            {
                return b == null;
            }

            if (b == null)
            {
                return false;
            }

            switch (a.GetType().Name)
            {
                case "Boolean":
                    if (b.GetType().Name.Equals("Boolean"))
                    {
                        return a == b;
                    }

                    return false;
                case "String":
                    if (b.GetType().Name.Equals("String"))
                    {
                        return a.Equals(b);
                    }

                    return false;
                case "Int64":
                    switch (b.GetType().Name)
                    {
                        case "Int64":
                            return (long) a == (long) b;
                        case "Double":
                            return ((double) b).Equals((double) a);
                        default: return false;
                    }
                case "Double":
                    switch (b.GetType().Name)
                    {
                        case "Double": return a.Equals(b);
                        case "Int64": return a.Equals((double) b);
                        default: return false;
                    }
                default: return a == b;
            }
        }

        bool _lt(object a, object b)
        {
            switch (a.GetType().Name)
            {
                case "String":
                    if (b.GetType().Name.Equals("String"))
                    {
                        return String.Compare(((string) a), (string) b, StringComparison.Ordinal) == -1;
                    }

                    break;
                case "Int64":
                    switch (b.GetType().Name)
                    {
                        case "Int64": return (long) a < (long) b;
                        case "Double": return (double) a < (double) b;
                    }

                    break;
                case "Double":
                    switch (b.GetType().Name)
                    {
                        case "Double": return (double) a < (double) b;
                        case "Int64": return (double) a < (double) b;
                    }

                    break;
            }

            throw new Exception("comparison error!");
        }

        bool _le(object a, object b)
        {
            switch (a.GetType().Name)
            {
                case "String":
                    if (b.GetType().Name.Equals("String"))
                    {
                        return string.CompareOrdinal((string) a, (string) b) <= 0;
                    }

                    break;
                case "Int64":
                    switch (b.GetType().Name)
                    {
                        case "Int64": return (long) a <= (long) b;
                        case "Double": return Convert.ToDouble(a) <= Convert.ToDouble(b);
                    }

                    break;
                case "Double":
                    switch (b.GetType().Name)
                    {
                        case "Double": return (double) a <= (double) b;
                        case "Int64": return Convert.ToDouble(a) <= Convert.ToDouble(b);
                    }

                    break;
            }

            throw new Exception("comparison error!");
        }
    }
}