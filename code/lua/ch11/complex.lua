local mt = {} -- metatable for complex numbers
mt.__add = function(x, y)
  return {
    a = x.a + y.a, 
    b = x.b + y.b
  }
end
mt.__sub = function(x, y)
  return {
    a = x.a - y.a, 
    b = x.b - y.b
  }
end
mt.__mul = function(x, y)
  return {
    a = x.a * y.a - x.b * y.b,
    b = x.b * y.a + x.a * y.b
  }
end
mt.__eq = function(x, y)
  return x.a == y.a and x.b == y.b
end
mt.__tostring = function(x)
  return "(" .. x.a .. " + " .. x.b + "i)" 
end

function newComplex(a, b)
  t = {a = a, b = b}
  setmetatable(t, mt)
  return t
end

local function assert(arg)
  if not arg then fail() end
end

local c1 = newComplex(3, 5)
local c2 = newComplex(3, 5)
local c3 = newComplex(4, 1)
local c4 = c2 + c3
assert(c1 == c2 and c2 ~= c3)
assert(c4.a == 7 and c4.b == 6)
