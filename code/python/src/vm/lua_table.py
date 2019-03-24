import collections
from vm.lua_value import LuaValue


class LuaTable:
    def __init__(self, narr, nrec):
        self.arr = None
        self.map = None
        self.metatable = None
        self.keys = None
        self.modified = False
        self.lastkey = None

        if narr > 0:
            self.arr = []
        if nrec > 0:
            self.map = {}

    def __len__(self):
        return len(self.arr) if self.arr is not None else 0

    def get(self, key):
        key = LuaValue.float2integer(key)

        if self.arr and isinstance(key, int):
            if 1 <= key <= len(self.arr):
                return self.arr[key-1]

        return self.map[key] if (self.map and key in self.map) else None

    def map_to_arr(self):
        if self.map:
            i = len(self.arr)
            while i in self.map:
                v = self.map[i]
                self.map.pop(i)
                self.arr.append(v)
                i += 1

    def put(self, key, val):
        assert(key is not None)
        assert(key is not float('NAN'))

        key = LuaValue.float2integer(key)
        if isinstance(key, int) and key >= 1:
            if not self.arr:
                self.arr = []
            arr_len = len(self.arr)
            if key <= arr_len:
                self.arr[key-1] = val
                if key == arr_len and val is None:
                    self.arr.pop(key)
                return
            if key == arr_len + 1:
                if self.map:
                    self.map.pop(key)
                if val is not None:
                    self.arr.append(val)
                    self.map_to_arr()
                return

        if val is not None:
            if not self.map:
                self.map = {key: val}
            else:
                self.map[key] = val
        else:
            self.map.pop(key)

    def dump(self):
        if self.arr:
            print(self.arr, end=' ')
        if self.map:
            print(self.map)

    def has_metafield(self, name):
        return self.metatable is not None and self.metatable.get(name) is not None

    def __str__(self):
        return str(self.arr) if self.arr else str(self.map)

    def init_keys(self):
        self.keys = collections.OrderedDict()
        key = None
        if self.arr is not None:
            for i in range(len(self.arr)):
                if self.arr[i] is not None:
                    self.keys[key] = i+1
                    key = i+1

        if self.map is not None:
            for k, v in self.map.items():
                if v is not None:
                    self.keys[key] = k
                    key = k

        self.lastkey = key

    def next_key(self, key):
        if self.keys is None or (key is None and self.modified):
            self.init_keys()
            self.modified = False

        nextkey = self.keys[key] if key in self.keys else None
        if nextkey is None and key is not None and key != self.lastkey:
            raise Exception('Invalid key to next')
        return nextkey
