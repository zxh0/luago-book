function range(i, j)
  local next = i - 1
  return function()
    next = next + 1
    if next > j then next = nil end
    return next
  end
end

iter = range(1, 10)
while true do
  local next = iter()
  if next == nil then break end
  print(next)
end

for n in range(1, 10) do
  print(n)
end
