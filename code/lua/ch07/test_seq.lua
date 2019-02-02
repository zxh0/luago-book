local t = {1,2,3}
-- print(#t)
t[3]=nil
-- print(#t)
t[2]=nil
-- print(#t)
if #t ~= 1 then
  fail()
else
  -- print("ok")
end
