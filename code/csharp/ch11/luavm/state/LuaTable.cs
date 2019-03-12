using System;
using System.Collections.Generic;
using System.Linq;
using LuaType = System.Int32;
using Math = luavm.number.Math;

namespace luavm.state
{
    public class LuaTable
    {
        internal LuaTable metatable;
        private List<object> arr;
        private Dictionary<object, object> _map;

        public LuaTable(int nArr, int nRec)
        {
//            var t = new LuaTable();
            if (nArr > 0)
            {
                arr = new List<object>(nArr);
            }

            if (nRec > 0)
            {
                _map = new Dictionary<object, object>(nRec);
            }
        }

        public object get(object key)
        {
            key = _floatToInteger(key);
            if (arr != null && key is long keyLong)
            {
                var idx = (int) keyLong;
                if (idx >= 1 && idx <= arr.Count)
                {
                    return arr[idx - 1];
                }
            }

            return _map != null ? _map.ContainsKey(key) ? _map[key] : null : null;
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
            for (int i = arr.Count() - 1; i >= 0; i--)
            {
                if (arr[i] == null)
                {
                    arr.Remove(i);
                }
            }
        }

        internal bool hasMetafield(string fieldName)
        {
            return metatable?.get(fieldName) != null;
        }

        private void _expandArray()
        {
            if (_map != null)
            {
                for (int idx = arr.Count + 1;; idx++)
                {
                    var val = _map[idx];
                    _map.Remove((long) idx);
                    if (val != null)
                    {
                        arr.Add(val);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }

        public int len()
        {
            return arr?.Count ?? 0;
        }

        public void put(object key, object val)
        {
            if (key == null) {
                throw new Exception("table index is nil!");
            }
            if (key is double keyDouble && Double.IsNaN(keyDouble)) {
                throw new Exception("table index is NaN!");
            }

            key = _floatToInteger(key);
            if (key is long keyLong)
            {
                var idx = (int)keyLong;
                if (idx >= 1) {
                    if (arr == null)
                    {
                        arr = new List<object>();
                    }

                    int arrLen = arr.Count;
                    if (idx <= arrLen) {
                        arr.RemoveAt(idx - 1);
                        arr.Insert(idx - 1, val);
                        
                        if (idx == arrLen && val == null) {
                            _shrinkArray();
                        }
                        return;
                    }
                    if (idx == arrLen + 1) {
                        if (_map != null) {
                            _map.Remove(key);
                        }
                        if (val != null) {
                            arr.Add(val);
                            _expandArray();
                        }
                        return;
                    }
                }
            }

            if (val != null) {
                if (_map == null) {
                    _map = new Dictionary<object, object>();
                }

                _map.Remove(key);
                _map.Add(key, val);
            } else {
                if (_map != null) {
                    _map.Remove(key);
                }
            }
        }
    }
}