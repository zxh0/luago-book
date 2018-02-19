local a, b, c
c = false     -- boolean
c = {1, 2, 3} -- table
c = "hello"   -- string
a = 3.14      -- number
b = a

print(type(nil))                     --> nil
print(type(true))                    --> boolean
print(type(3.14))                    --> number
print(type("Hello world"))           --> string
print(type({}))                      --> table
print(type(print))                   --> function
print(type(coroutine.create(print))) --> thread
print(type(io.stdin))                --> userdata
