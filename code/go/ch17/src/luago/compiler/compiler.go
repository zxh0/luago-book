package compiler

import "luago/binchunk"
import "luago/compiler/codegen"
import "luago/compiler/parser"

func Compile(source, chunk string) *binchunk.Prototype {
	ast := parser.Parse(source, chunk)
	return codegen.GenProto(ast)
}
