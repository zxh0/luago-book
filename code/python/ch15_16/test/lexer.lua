print("hello") -- short comment
print("world") --> another short comment
print() --[[ long comment ]]
--[===[
  another
  long comment
]===]

print("hello, \z
       world!") --> hello, world!

a = 'alo\n123"'
a = "alo\n123\""
a = '\97lo\10\04923"'
a = [[alo
123"]]
a = [==[
alo
123"]==]
a = 123.456e78
a = 'ação'
a = '\x66'
a = '\u{03a9}\u{03a8}'
a = [======aaa