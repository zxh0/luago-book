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
	upvals := make([]*upvalue, len(proto.Upvalues))
	return &closure{
		proto:  proto,
		upvals: upvals,
	}
}

func newGoClosure(f GoFunction, nUpvals int) *closure {
	upvals := make([]*upvalue, nUpvals)
	return &closure{
		goFunc: f,
		upvals: upvals,
	}
}
