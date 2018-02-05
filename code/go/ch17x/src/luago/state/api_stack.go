package state

// [-0, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_gettop
func (self *luaState) GetTop() int {
	return self.stack.top
}

// [-0, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_absindex
func (self *luaState) AbsIndex(idx int) int {
	return self.stack.absIndex(idx)
}

// [-0, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_checkstack
func (self *luaState) CheckStack(n int) bool {
	self.stack.check(n)
	return true // never fails
}

// [-n, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_pop
func (self *luaState) Pop(n int) {
	for i := 0; i < n; i++ {
		self.stack.pop()
	}
}

// [-0, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_copy
func (self *luaState) Copy(fromIdx, toIdx int) {
	val := self.stack.get(fromIdx)
	self.stack.set(toIdx, val)
}

// [-0, +1, –]
// http://www.lua.org/manual/5.3/manual.html#lua_pushvalue
func (self *luaState) PushValue(idx int) {
	val := self.stack.get(idx)
	self.stack.push(val)
}

// [-1, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_replace
func (self *luaState) Replace(idx int) {
	val := self.stack.pop()
	self.stack.set(idx, val)
}

// [-1, +1, –]
// http://www.lua.org/manual/5.3/manual.html#lua_insert
func (self *luaState) Insert(idx int) {
	self.Rotate(idx, 1)
}

// [-1, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_remove
func (self *luaState) Remove(idx int) {
	self.Rotate(idx, -1)
	self.Pop(1)
}

// [-0, +0, –]
// http://www.lua.org/manual/5.3/manual.html#lua_rotate
func (self *luaState) Rotate(idx, n int) {
	t := self.stack.top - 1           /* end of stack segment being rotated */
	p := self.stack.absIndex(idx) - 1 /* start of segment */
	var m int                         /* end of prefix */
	if n >= 0 {
		m = t - n
	} else {
		m = p - n - 1
	}
	self.stack.reverse(p, m)   /* reverse the prefix with length 'n' */
	self.stack.reverse(m+1, t) /* reverse the suffix */
	self.stack.reverse(p, t)   /* reverse the entire segment */
}

// [-?, +?, –]
// http://www.lua.org/manual/5.3/manual.html#lua_settop
func (self *luaState) SetTop(idx int) {
	newTop := self.stack.absIndex(idx)
	if newTop < 0 {
		panic("stack underflow!")
	}

	n := self.stack.top - newTop
	if n > 0 {
		for i := 0; i < n; i++ {
			self.stack.pop()
		}
	} else if n < 0 {
		for i := 0; i > n; i-- {
			self.stack.push(nil)
		}
	}
}
