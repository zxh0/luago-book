package codegen

import . "luago/binchunk"
import . "luago/compiler/ast"

func GenProto(chunk *Block) *Prototype {
	fd := &FuncDefExp{
		IsVararg: true,
		Block:    chunk,
	}

	fi := newFuncInfo(nil, fd)
	fi.addLocVar("_ENV")
	cgFuncDefExp(fi, fd, 0)
	return toProto(fi.subFuncs[0])
}
