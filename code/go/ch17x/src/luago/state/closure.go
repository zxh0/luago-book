package state

import . "luago/api"
import "luago/binchunk"

type upvalue struct {
	val *luaValue
}

type closure struct {
	proto  *binchunk.Prototype // lua closure
	goFunc GoFunction          // go closure
	upvals []*upvalue
}

func newLuaClosure(proto *binchunk.Prototype) *closure {
	c := &closure{proto: proto}
	if nUpvals := len(proto.Upvalues); nUpvals > 0 {
		c.upvals = make([]*upvalue, nUpvals)
	}
	return c
}

func newGoClosure(f GoFunction, nUpvals int) *closure {
	c := &closure{goFunc: f}
	if nUpvals > 0 {
		c.upvals = make([]*upvalue, nUpvals)
	}
	return c
}
