print(getmetatable("foo")) --> table: 0x7f8aab4050c0
print(getmetatable("bar")) --> table: 0x7f8aab4050c0
print(getmetatable(nil))   --> nil
print(getmetatable(false)) --> nil
print(getmetatable(100))   --> nil
print(getmetatable({}))    --> nil
print(getmetatable(print)) --> nil

t = {}
mt = {}
setmetatable(t, mt)
print(getmetatable(t) == mt)   --> true
debug.setmetatable(100, mt)
print(getmetatable(200) == mt) --> true


mt = {}
mt.__add = function(v1, v2)
  return vector(v1.x + v2.x, v1.y + v2.y)
end

function vector(x, y)
  local v = {x = x, y = y}
  setmetatable(v, mt)
  return v
end

v1 = vector(1, 2)
v2 = vector(3, 5)
v3 = v1 + v2
print(v3.x, v3.y) --> 4	7
