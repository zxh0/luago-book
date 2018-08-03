package com.github.zxh0.luago.state;

import com.github.zxh0.luago.number.LuaNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LuaTable {

    private List<Object> arr;
    private Map<Object, Object> map;

    LuaTable(int nArr, int nRec) {
        if (nArr > 0) {
            arr = new ArrayList<>(nArr);
        }
        if (nRec > 0) {
            map = new HashMap<>(nRec);
        }
    }

    int length() {
        return arr == null ? 0 : arr.size();
    }

    Object get(Object key) {
        key = floatToInteger(key);

        if (arr != null && key instanceof Long) {
            int idx = ((Long) key).intValue();
            if (idx >= 1 && idx <= arr.size()) {
                return arr.get(idx - 1);
            }
        }

        return map != null ? map.get(key) : null;
    }

    void put(Object key, Object val) {
        if (key == null) {
            throw new RuntimeException("table index is nil!");
        }
        if (key instanceof Double && ((Double) key).isNaN()) {
            throw new RuntimeException("table index is NaN!");
        }

        key = floatToInteger(key);
        if (key instanceof Long) {
            int idx = ((Long) key).intValue();
            if (idx >= 1) {
                if (arr == null) {
                    arr = new ArrayList<>();
                }

                int arrLen = arr.size();
                if (idx <= arrLen) {
                    arr.set(idx - 1, val);
                    if (idx == arrLen && val == null) {
                        shrinkArray();
                    }
                    return;
                }
                if (idx == arrLen + 1) {
                    if (map != null) {
                        map.remove(key);
                    }
                    if (val != null) {
                        arr.add(val);
                        expandArray();
                    }
                    return;
                }
            }
        }

        if (val != null) {
            if (map == null) {
                map = new HashMap<>();
            }
            map.put(key, val);
        } else {
            if (map != null) {
                map.remove(key);
            }
        }
    }

    private Object floatToInteger(Object key) {
        if (key instanceof Double) {
            Double f = (Double) key;
            if (LuaNumber.isInteger(f)) {
                return f.longValue();
            }
        }
        return key;
    }

    private void shrinkArray() {
        for (int i = arr.size() - 1; i >= 0; i--) {
            if (arr.get(i) == null) {
                arr.remove(i);
            }
        }
    }

    private void expandArray() {
        if (map != null) {
            for (int idx = arr.size() + 1; ; idx++) {
                Object val = map.remove((long) idx);
                if (val != null) {
                    arr.add(val);
                } else {
                    break;
                }
            }
        }
    }

}
