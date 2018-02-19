mymod = dofile("mymod.lua")
mymod.foo() --> foo
mymod.bar() --> bar

mymod = require "mymod"
mymod.foo() --> foo
mymod.bar() --> bar

package.loaded.mymod = "hello"
mymod = require "mymod"
print(mymod) --> hello

package.preload.mymod = function(modname)
  local loader = function(modname, extra)
    print("loading")
  end
  return loader, ""
end


function preloadSearcher(modname)
  if package.preload[modname] ~= nil then
    return package.preload[modname]
  else
    return "\n\tno field package.preload['" .. modname .. "']"
  end
end

function luaSearcher(modname)
  local file, err = package.searchpath(modname, package.path)
  if file ~= nil then
    return loadfile(file), modname
  else
    return err
  end
end

function require(modname)
  if package.loaded[nodname] ~= nil then
    return package.loaded[modname]
  end

  local err = "module '" .. name .. "' not found:"
  for i, searcher in ipairs(package.searchers) do
    local loader, extra = searcher(modname)
    if type(loader) == "function" then
      local mod = loader(modname, extra)
      package.loaded[modname] = mod
      return mod
    else
      err = err .. loader
    end
  end
  error(err)
end
