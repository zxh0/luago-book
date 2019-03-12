using System;

namespace luavm.number
{
    public class Parser
    {
        internal static Tuple<long, bool> ParseInteger(string str)
        {
            try
            {
                var i = Convert.ToInt64(str);
                return Tuple.Create(i, true);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return Tuple.Create(0L, false);
            }
        }

        internal static (double, bool) ParseFloat(string str)
        {
            try
            {
                var i = Convert.ToDouble(str);
                return (i, true);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return (0D, false);
            }
        }
    }
}