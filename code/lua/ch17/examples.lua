function f()
  local a, b = 1, 2; print(a, b)   -->	1	2
  local a, b = 3, 4; print(a, b)   -->	3	4
  do
    print(a, b)                    -->	3	4
    local a, b = 5, 6; print(a, b) -->	5	6
  end
  print(a, b)                      -->	3	4
end

f()