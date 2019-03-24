local mt = {}

function vector(x, y)
    local v = {x = x, y = y}
    setmetatable(v, mt)
    return v
end

mt.__add = function(v1, v2)
    return vector(v1.x + v2.x, v1.y + v2.y)
end
mt.__sub = function(v1, v2)
    return vector(v1.x - v2.x, v1.y - v2.y)
end
mt.__mul = function(v1, n)
    return vector(v1.x * n, v1.y * n)
end
mt.__div = function(v1, n)
    return vector(v1.x / n, v1.y / n)
end
mt.__len = function(v)
    return (v.x * v.x + v.y * v.y) ^ 0.5
end
mt.__eq = function(v1, v2)
    return v1.x == v2.x and v1.y == v2.y
end
mt.__index = function(v, k)
    if k == "print" then
        return function()
            print("[" .. v.x .. ", " .. v.y .. "]")
        end
    end
end
mt.__call = function(v)
    print("[" .. v.x .. ", " .. v.y .. "]")
end

v1 = vector(1, 2); v1:print()
v2 = vector(3, 4); v2:print()
v3 = v1 * 2;       v3:print()
v4 = v1 + v3;      v4:print()
print(#v2)
print(v1 == v2)
print(v2 == vector(3, 4))
v4()
