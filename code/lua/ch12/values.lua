-- PiL ch18
function values(t)
  local i = 0
  return function ()
    i = i + 1; return t[i]
  end
end

t = {10, 20, 30}
for v in values(t) do
  print(v)
end
