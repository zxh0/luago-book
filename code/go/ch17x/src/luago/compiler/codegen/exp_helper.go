package codegen

import . "luago/compiler/ast"

func isVarargOrFuncCall(exp Exp) bool {
	switch exp.(type) {
	case *VarargExp, *FuncCallExp:
		return true
	}
	return false
}

func removeTailNils(exps []Exp) []Exp {
	for n := len(exps) - 1; n >= 0; n-- {
		if _, ok := exps[n].(*NilExp); !ok {
			return exps[0 : n+1]
		}
	}
	return nil
}
