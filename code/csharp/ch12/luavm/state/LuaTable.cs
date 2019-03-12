using System;
using System.Collections.Generic;
using luavm.api;
using LuaType = System.Int32;
using Math = luavm.number.Math;

namespace luavm.state
{
    public class LuaTable
    {
        internal LuaTable Metatable;
        private List<object> _arr;

        private Dictionary<object, object> _map;
        private Dictionary<object, object> _keys;
        private object _lastKey;
        private bool _changed;

        public LuaTable(int nArr, int nRec)
        {
            if (nArr > 0)
            {
                _arr = new List<object>(nArr);
            }

            if (nRec > 0)
            {
                _map = new Dictionary<object, object>(nRec);
            }
        }

        public object NextKey(object key)
        {
            if (_keys is null || key is null && _changed)
            {
                InitKeys();
                _changed = false;
            }

            _keys.TryGetValue(key ?? Constant.NULL_ALIAS, out var nextKey);

            if (nextKey is null && key != null && key != _lastKey)
            {
                throw new Exception("invalid key to 'next'");
            }

            return nextKey;
        }

        private void InitKeys()
        {
            if (_keys is null)
            {
                _keys = new Dictionary<object, object>();
            }
            else
            {
                _keys.Clear();
            }

            object key = Constant.NULL_ALIAS;
            if (_arr != null)
            {
                for (var i = 0; i < _arr.Count; i++)
                {
                    if (_arr[i] == null) continue;
                    long nextKey = i + 1;
                    _keys.Add(key, nextKey);
                    key = nextKey;
                }
            }

            if (_map != null)
            {
                foreach (var k in _map.Keys)
                {
                    var v = _map[k];
                    if (v is null) continue;
                    _keys.Add(key, k);
                    key = k;
                }
            }

            _lastKey = key;
        }

        public object Get(object key)
        {
            try
            {
                key = _floatToInteger(key);
                if (_arr == null || !(key is long keyLong))
                {
                    return _map?[key];
                }

                var idx = (int) keyLong;
                if (idx >= 1 && idx <= _arr.Count)
                {
                    return _arr[idx - 1];
                }

                return _map?[key];
            }
            catch (Exception e)
            {
                return null;
            }
        }

        private static object _floatToInteger(object key)
        {
            if (key is double dk)
            {
                return Math.FloatToInteger(dk).Item1;
            }

            return key;
        }

        private void _shrinkArray()
        {
            for (var i = _arr.Count - 1; i >= 0; i--)
            {
                if (_arr[i] == null)
                {
                    _arr.Remove(i);
                }
            }
        }

        internal bool HasMetafield(string fieldName)
        {
            return Metatable?.Get(fieldName) != null;
        }

        private void _expandArray()
        {
            if (_map == null) return;
            for (var idx = _arr.Count + 1;; idx++)
            {
                var val = _map[idx];
                _map.Remove((long) idx);
                if (val != null)
                {
                    _arr.Add(val);
                }
                else
                {
                    break;
                }
            }
        }

        public int Len()
        {
            return _arr?.Count ?? 0;
        }

        public void Put(object key, object val)
        {
            switch (key)
            {
                case null:
                    throw new Exception("table index is nil!");
                case double keyDouble when double.IsNaN(keyDouble):
                    throw new Exception("table index is NaN!");
            }

            key = _floatToInteger(key);
            if (key is long keyLong)
            {
                var idx = (int) keyLong;
                if (idx >= 1)
                {
                    if (_arr == null)
                    {
                        _arr = new List<object>();
                    }

                    var arrLen = _arr.Count;
                    if (idx <= arrLen)
                    {
                        _arr.RemoveAt(idx - 1);
                        _arr.Insert(idx - 1, val);

                        if (idx == arrLen && val == null)
                        {
                            _shrinkArray();
                        }

                        return;
                    }

                    if (idx == arrLen + 1)
                    {
                        _map?.Remove(key);
                        if (val == null) return;
                        _arr.Add(val);
                        _expandArray();
                        return;
                    }
                }
            }

            if (val != null)
            {
                if (_map == null)
                {
                    _map = new Dictionary<object, object>();
                }

                _map.Remove(key);
                _map.Add(key, val);
            }
            else
            {
                _map?.Remove(key);
            }
        }
    }
}