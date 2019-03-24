
function test(a)
    if a >= 90 then
        return 'A'
    elseif a >= 80 then
        return 'B'
    elseif a >= 70 then
        return 'C'
    else
        return 'D'
    end
end

print(test(100), test(90), test(80), test(70), test(60), test(30))




print('hello world')


a = 1
print(a)

a = a+1
print(a)

b = a*2
print(b)

print(a < b)
print(b, b==a*2, b<=a*2, b/3, b//3, b%3, 2^53, 2~=2, 2~=3)
print(b<<2, b>>2, b&4, b&3)

print(4.1e-3, 5E+20, 1==1.0, 0xff)
print(2 and 3)
print(0 and 3)
print(nil and 7)
print(false and 7)
print(1 or 2, 3 or 4, false or 5, true or 6, nil or 7, 8 or nil)

print(3 or 4)
a = 3
b = 4
print(a or b)

print(not 2)

a = 'abc'
print(a)
print(#a)
b = 'abc' .. 'def'
print(b)
c = 'xxx'
d = b .. c
print(d)

a = {}
k = "x"
a[k] = 10
a[20] = "great"
print(a["x"])
k = 20
print(a[k])
a["x"] = a["x"]+1
print(a["x"])

a = {}
a["x"] = 10
b = a
print(b["x"])
b["x"]=20
print(a["x"])
a = nil
b = nil

a = {1, 2, 3, 4}
print(a[0], a[1], a[2], a[3], a[4])

a = {}
a.x = 10
print(a.x)
print(a.y)

days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}
print(days[4])


function add(a, b)
    return a+b
end

c = add(2, 3)
print(c)

a, b = 12, 34
print(add(a, b))



a, b = 1, 2
if a > 1 then
    c = a
else
    c = b
end

print(c)

function test(a)
    if a >= 90 then
        return 'A'
    elseif a >= 80 then
        return 'B'
    elseif a >= 70 then
        return 'C'
    else
        return 'D'
    end
end

print(test(100), test(90), test(80), test(70), test(60), test(30))


i = 1
sum = 0
while i <= 100 do
    sum = sum + i
    i = i + 1
end

print(sum)



i = 1
sum = 0

repeat
    sum = sum + i
    i = i + 1
until i > 100

print(sum)


sum = 0
for i = 0, 100, 1 do
    sum = sum + i
    if i == 10 then break end
end
print(sum)

function add(...)
    local s = 0
    for _, v in ipairs{...} do
        s = s + v
    end
    return s
end

print(add(3, 4, 10, 25, 12))



a = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}
for i = 1, 10 do a[i] = i*2 end
print(a[5])


function test()
    function inner(a)
        return a+1
    end
    print(inner(3))
end
test()
