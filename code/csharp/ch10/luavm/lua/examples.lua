f, g = pcall, print; g("hello")   --> hello
t = {pcall, print}; t[2]("hello") --> hello
pcall(print, "hello")             --> hello
--return function (x) print(x) end


function add(x, y) return x + y end
add = function(x, y) return x + y end


--[[
x=1
function g () { echo $x ; x=2 ; }
function f () { local x=3 ; g ; }
f       # 3
echo $x # 1
]]
x = 1
function g() print(x); x = 2 end
function f() local x = 3; g() end
f()      -- 1
print(x) -- 2


local u,v,w
local function f()
  u = v
end


local u,v,w
local function f()
  local function g()
    x = y 
  end
end



-- function f1()
--   local v1, v2
--   function f2()
--     local v3, v4
--     function f3()
--       local v5, v6
--     end
--   end
-- end