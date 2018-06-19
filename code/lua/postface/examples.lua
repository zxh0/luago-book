-- label & goto
for i=1,10 do
  if i ~= 5 then
    goto continue
  end
  print(i)
  ::continue::
end


-- weak table
a = {}
setmetatable(a, {__mode = "k"})
key = {}; a[key] = 1
key = {}; a[key] = 2
collectgarbage()
for k, v in pairs(a) do print(v) end --> 2


-- finalizer
o = {x = "hi"}
setmetatable(o, {__gc = function (o) print(o.x) end})
o = nil
collectgarbage() --> hi


-- userdata
f = io.tmpfile()
print(type(f)) --> userdata
mt = getmetatable(f)
for k,v in pairs(mt) do print(k,v) end
--[[
close		function: 0x106e46280
seek		function: 0x106e478d0
lines		function: 0x106e47830
__name		FILE*
read		function: 0x106e47880
setvbuf		function: 0x106e47990
__index		table: 0x7f889c404580
__tostring	function: 0x106e47af0
__gc		function: 0x106e47a90
write		function: 0x106e47a30
flush		function: 0x106e477d0
]]