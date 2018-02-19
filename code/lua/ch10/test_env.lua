local function assert(arg)
  if not arg then fail() end
end

assert(_ENV ~= nil)
assert(foo == nil)

foo = "bar"
assert(foo == "bar")
assert(_ENV["foo"] == "bar")
