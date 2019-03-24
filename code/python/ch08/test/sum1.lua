local sum = 0
local x = 1
for i = 1, 100 do
    if i % 2 == 0 then
        sum = sum + i
    else
        sum = sum + x
        x = x * 2
    end
end
