using System;
using System.Collections.Generic;
using System.Linq;
using LuaType = System.Int32;
using Math = luavm.number.Math;

namespace luavm.state
{
    public struct LuaTable
    {
        private object[] arr;
        private Dictionary<object, object> _map;

        public static LuaTable newLuaTable(int nArr, int nRec)
        {
            var t = new LuaTable();
            if (nArr > 0)
            {
                t.arr = new object[nArr];
            }

            if (nRec > 0)
            {
                t._map = new Dictionary<object, object>(nRec);
            }

            return t;
        }

        public object get(object key)
        {
            key = _floatToInteger(key);
            if (LuaValue.isInteger(key))
            {
                if (arr != null)
                {
                    var idx = LuaValue.toInteger(key);
                    if (idx >= 1 && idx <= arr.Length)
                    {
                        return arr[idx - 1];
                    }
                }
            }

            return _map.ContainsKey(key) ? _map[key] : null;
        }

        private static object _floatToInteger(object key)
        {
            if (LuaValue.isFloat(key))
            {
                var f = LuaValue.toFloat(key);
                return Math.FloatToInteger(f).Item1;
            }

            return key;
        }

        private void _shrinkArray()
        {
            for (var i = arr.Length - 1; i >= 0; i--)
            {
                if (arr[i] == null)
                {
                    Array.Copy(arr, 0, arr, 0, i);
                }
            }
        }

        private void _expandArray()
        {
            for (var idx = arr.Length + 1; true; idx++)
            {
                if (_map != null && _map.ContainsKey(idx))
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

        public void put(object key, object val)
        {
            if (key == null)
            {
                throw new Exception("table index is nil!");
            }

            if (LuaValue.isFloat(key) && double.IsNaN(LuaValue.toFloat(key)))
            {
                throw new Exception("table index is NaN!");
            }

            key = _floatToInteger(key);

            if (LuaValue.isInteger(key))
            {
                var idx = LuaValue.toInteger(key);
                if (idx >= 1)
                {
                    var arrLen = arr?.Length ?? -1;
                    if (idx <= arrLen)
                    {
                        if (arr != null) arr[idx - 1] = val;
                        if (idx == arrLen && val == null)
                        {
                            _shrinkArray();
                        }

                        return;
                    }

                    if (idx == arrLen + 1)
                    {
                        _map?.Remove(idx);

                        if (val != null)
                        {
                            if (arr == null)
                            {
                                var b = new List<object> {val};
                                arr = b.ToArray();
                                _expandArray();
                            }
                            else
                            {
                                var b = arr.ToList();
                                b.Add(val);
                                arr = b.ToArray();
                                _expandArray();
                            }
                        }

                        return;
                    }
                }
            }

            if (val != null)
            {
                if (_map == null)
                {
                    _map = new Dictionary<object, object>(8);
                }

                if (!_map.ContainsKey(key))
                {
                    _map.Add(key, val);
                }
                else
                {
                    _map[key] = val;
                }
            }
            else
            {
                _map.Remove(key);
            }
        }
    }
}