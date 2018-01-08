package state

import . "luago/api"

// [-0, +1, m]
// http://www.lua.org/manual/5.3/manual.html#lua_newtable
func (self *luaState) NewTable() {
	self.CreateTable(0, 0)
}

// [-0, +1, m]
// http://www.lua.org/manual/5.3/manual.html#lua_createtable
func (self *luaState) CreateTable(nArr, nRec int) {
	t := newLuaTable(nArr, nRec)
	self.stack.push(t)
}

// [-1, +1, e]
// http://www.lua.org/manual/5.3/manual.html#lua_gettable
func (self *luaState) GetTable(idx int) LuaType {
	t := self.stack.get(idx)
	k := self.stack.pop()
	return self.getTable(t, k)
}

// [-0, +1, e]
// http://www.lua.org/manual/5.3/manual.html#lua_getfield
func (self *luaState) GetField(idx int, k string) LuaType {
	t := self.stack.get(idx)
	return self.getTable(t, k)
}

// [-0, +1, e]
// http://www.lua.org/manual/5.3/manual.html#lua_geti
func (self *luaState) GetI(idx int, i int64) LuaType {
	t := self.stack.get(idx)
	return self.getTable(t, i)
}

// push(t[k])
func (self *luaState) getTable(t, k luaValue) LuaType {
	if tbl, ok := t.(*luaTable); ok {
		v := tbl.get(k)
		self.stack.push(v)
		return typeOf(v)
	}

	panic("not a table!") // todo
}
