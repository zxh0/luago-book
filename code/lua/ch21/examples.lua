co = coroutine.create(function() print("hello") end)
print(type(co)) --> thread


main = coroutine.running()
print(type(main))             --> thread
print(coroutine.status(main)) --> running


co = coroutine.create(function()
  print(coroutine.status(co)) --> running
  coroutine.resume(coroutine.create(function()
    print(coroutine.status(co)) --> normal
  end))
end)
print(coroutine.status(co)) --> suspended
coroutine.resume(co)
print(coroutine.status(co)) --> dead


co = coroutine.create(function(a, b, c)
  print(a, b, c)
  while true do 
    print(coroutine.yield())
  end
end)
coroutine.resume(co, 1, 2, 3) --> 1 2 3
coroutine.resume(co, 4, 5, 6) --> 4 5 6
coroutine.resume(co, 7, 8, 9) --> 7 8 9


co = coroutine.create(function()
  for k, v in pairs({"a", "b", "c"}) do
    coroutine.yield(k, v)
  end
  return "d", 4
end)
print(coroutine.resume(co)) --> true  1  a
print(coroutine.resume(co)) --> true  2  b
print(coroutine.resume(co)) --> true  3  c
print(coroutine.resume(co)) --> true  d  4
print(coroutine.resume(co)) --> false cannot resume dead coroutin
