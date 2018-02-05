package compiler

import "luago/binchunk"
import "luago/compiler/codegen"
import "luago/compiler/parser"

func Compile(source, chunk string) *binchunk.Prototype {
	ast := parser.Parse(source, chunk)
	proto := codegen.GenProto(ast)
	setSource(proto, source) // todo
	return proto
}

// todo
func setSource(proto *binchunk.Prototype, source string) {
	proto.Source = "@" + source
	for _, f := range proto.Protos {
		setSource(f, source)
	}
}
