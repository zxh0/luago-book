local mt = {}

function vector(x, y)
    local v = {x = x, y = y}
    setmetatable(v, mt)
    return v
end

mt.__add = function(v1, v2)
    return vector(v1.x + v2.x, v1.y + v2.y)
end

v1 = vector(1, 2)
v2 = vector(3, 5)

v3 = v1 + v2
print(v3.x, v3.y)
