package state

// [-0, +1, –]
// http://www.lua.org/manual/5.3/manual.html#lua_pushnil
func (self *luaState) PushNil() {
	self.stack.push(nil)
}

// [-0, +1, –]
// http://www.lua.org/manual/5.3/manual.html#lua_pushboolean
func (self *luaState) PushBoolean(b bool) {
	self.stack.push(b)
}

// [-0, +1, –]
// http://www.lua.org/manual/5.3/manual.html#lua_pushinteger
func (self *luaState) PushInteger(n int64) {
	self.stack.push(n)
}

// [-0, +1, –]
// http://www.lua.org/manual/5.3/manual.html#lua_pushnumber
func (self *luaState) PushNumber(n float64) {
	self.stack.push(n)
}

// [-0, +1, m]
// http://www.lua.org/manual/5.3/manual.html#lua_pushstring
func (self *luaState) PushString(s string) {
	self.stack.push(s)
}
