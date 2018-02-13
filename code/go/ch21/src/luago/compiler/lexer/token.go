package lexer

// token kind
const (
	TOKEN_EOF         = iota           // end-of-file
	TOKEN_VARARG                       // ...
	TOKEN_SEP_SEMI                     // ;
	TOKEN_SEP_COMMA                    // ,
	TOKEN_SEP_DOT                      // .
	TOKEN_SEP_COLON                    // :
	TOKEN_SEP_LABEL                    // ::
	TOKEN_SEP_LPAREN                   // (
	TOKEN_SEP_RPAREN                   // )
	TOKEN_SEP_LBRACK                   // [
	TOKEN_SEP_RBRACK                   // ]
	TOKEN_SEP_LCURLY                   // {
	TOKEN_SEP_RCURLY                   // }
	TOKEN_OP_ASSIGN                    // =
	TOKEN_OP_MINUS                     // - (sub or unm)
	TOKEN_OP_WAVE                      // ~ (bnot or bxor)
	TOKEN_OP_ADD                       // +
	TOKEN_OP_MUL                       // *
	TOKEN_OP_DIV                       // /
	TOKEN_OP_IDIV                      // //
	TOKEN_OP_POW                       // ^
	TOKEN_OP_MOD                       // %
	TOKEN_OP_BAND                      // &
	TOKEN_OP_BOR                       // |
	TOKEN_OP_SHR                       // >>
	TOKEN_OP_SHL                       // <<
	TOKEN_OP_CONCAT                    // ..
	TOKEN_OP_LT                        // <
	TOKEN_OP_LE                        // <=
	TOKEN_OP_GT                        // >
	TOKEN_OP_GE                        // >=
	TOKEN_OP_EQ                        // ==
	TOKEN_OP_NE                        // ~=
	TOKEN_OP_LEN                       // #
	TOKEN_OP_AND                       // and
	TOKEN_OP_OR                        // or
	TOKEN_OP_NOT                       // not
	TOKEN_KW_BREAK                     // break
	TOKEN_KW_DO                        // do
	TOKEN_KW_ELSE                      // else
	TOKEN_KW_ELSEIF                    // elseif
	TOKEN_KW_END                       // end
	TOKEN_KW_FALSE                     // false
	TOKEN_KW_FOR                       // for
	TOKEN_KW_FUNCTION                  // function
	TOKEN_KW_GOTO                      // goto
	TOKEN_KW_IF                        // if
	TOKEN_KW_IN                        // in
	TOKEN_KW_LOCAL                     // local
	TOKEN_KW_NIL                       // nil
	TOKEN_KW_REPEAT                    // repeat
	TOKEN_KW_RETURN                    // return
	TOKEN_KW_THEN                      // then
	TOKEN_KW_TRUE                      // true
	TOKEN_KW_UNTIL                     // until
	TOKEN_KW_WHILE                     // while
	TOKEN_IDENTIFIER                   // identifier
	TOKEN_NUMBER                       // number literal
	TOKEN_STRING                       // string literal
	TOKEN_OP_UNM      = TOKEN_OP_MINUS // unary minus
	TOKEN_OP_SUB      = TOKEN_OP_MINUS
	TOKEN_OP_BNOT     = TOKEN_OP_WAVE
	TOKEN_OP_BXOR     = TOKEN_OP_WAVE
)

var keywords = map[string]int{
	"and":      TOKEN_OP_AND,
	"break":    TOKEN_KW_BREAK,
	"do":       TOKEN_KW_DO,
	"else":     TOKEN_KW_ELSE,
	"elseif":   TOKEN_KW_ELSEIF,
	"end":      TOKEN_KW_END,
	"false":    TOKEN_KW_FALSE,
	"for":      TOKEN_KW_FOR,
	"function": TOKEN_KW_FUNCTION,
	"goto":     TOKEN_KW_GOTO,
	"if":       TOKEN_KW_IF,
	"in":       TOKEN_KW_IN,
	"local":    TOKEN_KW_LOCAL,
	"nil":      TOKEN_KW_NIL,
	"not":      TOKEN_OP_NOT,
	"or":       TOKEN_OP_OR,
	"repeat":   TOKEN_KW_REPEAT,
	"return":   TOKEN_KW_RETURN,
	"then":     TOKEN_KW_THEN,
	"true":     TOKEN_KW_TRUE,
	"until":    TOKEN_KW_UNTIL,
	"while":    TOKEN_KW_WHILE,
}
