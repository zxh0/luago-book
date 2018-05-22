package compiler

import "luago/binchunk"
import "luago/compiler/codegen"
import "luago/compiler/parser"

func Compile(chunk, chunkName string) *binchunk.Prototype {
	ast := parser.Parse(chunk, chunkName)
	return codegen.GenProto(ast)
}
