function ipairs(t)
  local i = 0
  return function()
    i = i + 1
    if t[i] == nil then
      return nil, nil
    else
      return i, t[i]
    end
  end
end

t = {10, 20, 30}
iter = ipairs(t)
while true do
  local i, v = iter()
  if i == nil then
    break
  end
  print(i, v)
end

t = {10, 20, 30}
for i, v in ipairs(t) do
  print(i, v)
end


function pairs(t)
  local k, v
  return function ()
    k, v = next(t, k)
    return k, v
  end
end

t = {a=10, b=20, c=30}
for k, v in pairs(t) do
  print(k, v)
end


t = {a=10, b=20, c=30}
for k, v in next, t, nil do
  print(k, v)
end


function pairs(t)
  return next, t, nil
end
t = {a=10, b=20, c=30}
for k, v in pairs(t) do
  print(k, v)
end


function inext(t, i)
  local nextIdx = i + 1
  local nextVal = t[nextIdx] 
  if nextVal == nil then
    return nil
  else 
    return nextIdx, nextVal
  end
end
t = {10, 20, 30}
for i, v in inext, t, 0 do
  print(i, v)
end


--[[
function next(table, key)
  if key == nil then
    nextKey = table.firstKey()
  else
    nextKey = table.nextKey(key)
  end
  if nextKey ~= nil then
    return nextKey, table[nextKey]
  else
    return nil
  end
end
--]]
