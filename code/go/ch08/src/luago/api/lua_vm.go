package api

type LuaVM interface {
	LuaState
	PC() int
	AddPC(n int)
	Fetch() uint32
	GetConst(idx int)
	GetRK(rk int)
	RegisterCount() int
	LoadProto(idx int)
	LoadVararg(n int)
}
