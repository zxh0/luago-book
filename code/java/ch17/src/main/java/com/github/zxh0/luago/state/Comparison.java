package com.github.zxh0.luago.state;

class Comparison {

    static boolean eq(Object a, Object b, LuaStateImpl ls) {
        if (a == null) {
            return b == null;
        } else if (a instanceof Boolean || a instanceof String) {
            return a.equals(b);
        } else if (a instanceof Long) {
            return a.equals(b) ||
                    (b instanceof Double && b.equals(((Long) a).doubleValue()));
        } else if (a instanceof Double) {
            return a.equals(b) ||
                    (b instanceof Long && a.equals(((Long) b).doubleValue()));
        } else if (a instanceof LuaTable) {
            if (b instanceof LuaTable && a != b && ls != null) {
                Object mm = ls.getMetamethod(a, b, "__eq");
                if (mm != null) {
                    return LuaValue.toBoolean(ls.callMetamethod(a, b, mm));
                }
            }
            return a == b;
        } else {
            return a == b;
        }
    }

    static boolean lt(Object a, Object b, LuaStateImpl ls) {
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b) < 0;
        }
        if (a instanceof Long) {
            if (b instanceof Long) {
                return ((Long) a) < ((Long) b);
            } else if (b instanceof Double) {
                return ((Long) a).doubleValue() < ((Double) b);
            }
        }
        if (a instanceof Double) {
            if (b instanceof Double) {
                return ((Double) a) < ((Double) b);
            } else if (b instanceof Long) {
                return ((Double) a) < ((Long) b).doubleValue();
            }
        }
        Object mm = ls.getMetamethod(a, b, "__lt");
        if (mm != null) {
            return LuaValue.toBoolean(ls.callMetamethod(a, b, mm));
        }
        throw new RuntimeException("comparison error!");
    }

    static boolean le(Object a, Object b, LuaStateImpl ls) {
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b) <= 0;
        }
        if (a instanceof Long) {
            if (b instanceof Long) {
                return ((Long) a) <= ((Long) b);
            } else if (b instanceof Double) {
                return ((Long) a).doubleValue() <= ((Double) b);
            }
        }
        if (a instanceof Double) {
            if (b instanceof Double) {
                return ((Double) a) <= ((Double) b);
            } else if (b instanceof Long) {
                return ((Double) a) <= ((Long) b).doubleValue();
            }
        }
        Object mm = ls.getMetamethod(a, b, "__le");
        if (mm != null) {
            return LuaValue.toBoolean(ls.callMetamethod(a, b, mm));
        }
        mm = ls.getMetamethod(b, a, "__lt");
        if (mm != null) {
            return LuaValue.toBoolean(ls.callMetamethod(b, a, mm));
        }
        throw new RuntimeException("comparison error!");
    }

}
