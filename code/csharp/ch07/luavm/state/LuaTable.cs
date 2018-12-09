using System;
using System.Collections.Generic;
using System.Linq;
using LuaType = System.Int32;
using Math = luavm.number.Math;

namespace luavm.state
{
    public struct LuaTable
    {
        private LuaValue[] arr;
        private Dictionary<object, LuaValue> _map;

        public static LuaTable newLuaTable(int nArr, int nRec)
        {
            var t = new LuaTable();
            if (nArr > 0)
            {
                t.arr = new LuaValue[nArr];
            }

            if (nRec > 0)
            {
                t._map = new Dictionary<object, LuaValue>(nRec);
            }

            return t;
        }

        public LuaValue get(LuaValue key)
        {
            key = _floatToInteger(key);
            if (key.isInteger())
            {
                var idx = key.toInteger();
                if (idx >= 1 && idx <= arr.Length)
                {
                    return new LuaValue(arr[idx - 1]);
                }
            }

            return new LuaValue(_map[key.value]);
        }

        LuaValue _floatToInteger(LuaValue key)
        {
            if (key.isFloat())
            {
                var f = key.toFloat();
                return new LuaValue(Math.FloatToInteger(f).Item1);
            }

            return key;
        }

        void _shrinkArray()
        {
            for (var i = arr.Length - 1; i >= 0; i--)
            {
                if (arr[i] == null)
                {
                    Array.Copy(arr, 0, arr, 0, i);
                }
            }
        }

        void _expandArray()
        {
            for (var idx = arr.Length + 1; true; idx++)
            {
                if (_map.ContainsKey(idx))
                {
                    var val = _map.Values.ElementAt(idx);
                    _map.Remove(idx);
                    var b = arr.ToList();
                    b.Add(val);
                    arr = b.ToArray();
                }
                else
                {
                    break;
                }
            }
        }

        public int len()
        {
            return arr.Length;
        }

        public void put(LuaValue key, LuaValue val)
        {
            if (key == null)
            {
                throw new Exception("table index is nil!");
            }

            if (key.isFloat() && double.IsNaN(key.toFloat()))
            {
                throw new Exception("table index is NaN!");
            }

            key = _floatToInteger(key);

            if (key.isInteger())
            {
                var idx = key.toInteger();
                if (idx >= 1)
                {
                    var arrLen = arr.Length;
                    if (idx <= arrLen)
                    {
                        arr[idx - 1] = val;
                        if (idx == arrLen && val.value == null)
                        {
                            _shrinkArray();
                        }

                        return;
                    }

                    if (idx == arrLen + 1)
                    {
                        _map.Remove(idx);
                        if (val != null)
                        {
                            var b = arr.ToList();
                            b.Add(val);
                            arr = b.ToArray();
                            _expandArray();
                        }

                        return;
                    }
                }
            }

            if (val != null)
            {
                if (_map == null)
                {
                    _map = new Dictionary<object, LuaValue>(8);
                }

                _map.Add(key.value, val);
            }
            else
            {
                _map.Remove(key.value);
            }
        }
    }
}