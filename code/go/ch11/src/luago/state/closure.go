package state

import . "luago/api"
import "luago/binchunk"

type closure struct {
	proto  *binchunk.Prototype // lua closure
	goFunc GoFunction          // go closure
	upvals []*luaValue
}

func newLuaClosure(proto *binchunk.Prototype) *closure {
	upvals := make([]*luaValue, len(proto.Upvalues))
	return &closure{
		proto:  proto,
		upvals: upvals,
	}
}

func newGoClosure(f GoFunction, nUpvals int) *closure {
	upvals := make([]*luaValue, nUpvals)
	return &closure{
		goFunc: f,
		upvals: upvals,
	}
}
