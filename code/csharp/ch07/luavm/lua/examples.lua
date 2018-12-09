local t = {} -- empty table
local p = {x= 100, y= 200}

t[false] = nil; assert(t[false] == nil)
t["pi"] = 3.14; assert(t["pi"] == 3.14)
t[t] = "table"; assert(t[t] == "table")
t[10] = assert; assert(t[10] == assert)

local arr = {"a", "b", "c", nil, "e"}
assert(arr[1] == "a")
assert(arr[2] == "b")
assert(arr[3] == "c")
assert(arr[4] == nil)
assert(arr[5] == "e")

local seq = {"a", "b", "c", "d", "e"}
assert(#seq == 5)

local t = {}
t[6] = "foo";   assert(t[6.0] == "foo")
t[7.0] = "foo"; assert(t[7] == "foo")
t["8"] = "bar"; assert(t[8] == nil)
t[9] = "bar";   assert(t["9"] == nil)

local t = {1, 2, 3}
t[5] = 5
assert(#t == 3)
t[4] = 4
assert(#t == 5)
