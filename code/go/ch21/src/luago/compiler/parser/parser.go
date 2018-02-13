package parser

import . "luago/compiler/ast"
import . "luago/compiler/lexer"

/* recursive descent parser */

func Parse(source, chunk string) *Block {
	lexer := NewLexer(source, chunk)
	block := parseBlock(lexer)
	lexer.NextTokenOfKind(TOKEN_EOF)
	return block
}
