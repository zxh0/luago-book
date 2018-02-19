print(math.type(100))          --> integer
print(math.type(3.14))         --> float
print(math.type("100"))        --> nil
print(math.tointeger(100.0))   --> 100
print(math.tointeger("100.0")) --> 100
print(math.tointeger(3.14))    --> nil


t = table.pack(1, 2, 3, 4, 5); print(table.unpack(t)) --> 1 2 3 4 5
table.move(t, 4, 5, 1);        print(table.unpack(t)) --> 4 5 3 4 5
table.insert(t, 3, 2);         print(table.unpack(t)) --> 4 5 2 3 4 5
table.remove(t, 2);            print(table.unpack(t)) --> 4 2 3 4 5
table.sort(t);                 print(table.unpack(t)) --> 2 3 4 4 5
print(table.concat(t, ","))                           --> 2,3,4,4,5


print(string.len("abc"))            --> 3
print(string.rep("a", 3, ","))      --> a,a,a
print(string.reverse("abc"))        --> cba
print(string.lower("ABC"))          --> abc
print(string.upper("abc"))          --> ABC
print(string.sub("abcdefg", 3, 5))  --> cde
print(string.byte("abcdefg", 3, 5)) --> 99 100 101
print(string.char(99, 100, 101))    --> cde

s = "aBc"
print(s:len())       --> 2
print(s:rep(3, ",")) --> aBc,aBc,aBc
print(s:reverse())   --> cBa
print(s:upper())     --> ABC
print(s:lower())     --> abc
print(s:sub(1, 2))   --> aB
print(s:byte(1, 2))  --> 97 66

print(string.len("你好，世界！"))			--> 18
print(utf8.len("你好，世界！"))			--> 6
print(utf8.char(0x4f60, 0x597d))		--> 你好
print("\u{4f60}\u{597d}")				--> 你好
print(utf8.offset("你好，世界！", 2))		--> 4
print(utf8.offset("你好，世界！", 5))		--> 13
print(utf8.codepoint("你好，世界！", 4))	--> 22909
print(utf8.codepoint("你好，世界！", 13))	--> 30028
for p, c in utf8.codes("你好，世界！") do
  print(p, c)
end

print(os.time()) --> 1518320879
print(os.time{year=2018, month=2, day=14,
  hour=12, min=30, sec=30}) --> 1518582630

print(os.date()) --> Sun Feb 11 11:49:28 2018
t = os.date("*t", 1518582630)
print(t.year)  --> 2018
print(t.month) --> 02
print(t.day)   --> 14
print(t.hour)  --> 12
print(t.min)   --> 30
print(t.sec)   --> 30
