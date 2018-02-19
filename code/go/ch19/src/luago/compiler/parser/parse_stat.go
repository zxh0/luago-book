package parser

import . "luago/compiler/ast"
import . "luago/compiler/lexer"

var _statEmpty = &EmptyStat{}

/*
stat ::=  ‘;’
	| break
	| ‘::’ Name ‘::’
	| goto Name
	| do block end
	| while exp do block end
	| repeat block until exp
	| if exp then block {elseif exp then block} [else block] end
	| for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
	| for namelist in explist do block end
	| function funcname funcbody
	| local function Name funcbody
	| local namelist [‘=’ explist]
	| varlist ‘=’ explist
	| functioncall
*/
func parseStat(lexer *Lexer) Stat {
	switch lexer.LookAhead() {
	case TOKEN_SEP_SEMI:
		return parseEmptyStat(lexer)
	case TOKEN_KW_BREAK:
		return parseBreakStat(lexer)
	case TOKEN_SEP_LABEL:
		return parseLabelStat(lexer)
	case TOKEN_KW_GOTO:
		return parseGotoStat(lexer)
	case TOKEN_KW_DO:
		return parseDoStat(lexer)
	case TOKEN_KW_WHILE:
		return parseWhileStat(lexer)
	case TOKEN_KW_REPEAT:
		return parseRepeatStat(lexer)
	case TOKEN_KW_IF:
		return parseIfStat(lexer)
	case TOKEN_KW_FOR:
		return parseForStat(lexer)
	case TOKEN_KW_FUNCTION:
		return parseFuncDefStat(lexer)
	case TOKEN_KW_LOCAL:
		return parseLocalAssignOrFuncDefStat(lexer)
	default:
		return parseAssignOrFuncCallStat(lexer)
	}
}

// ;
func parseEmptyStat(lexer *Lexer) *EmptyStat {
	lexer.NextTokenOfKind(TOKEN_SEP_SEMI)
	return _statEmpty
}

// break
func parseBreakStat(lexer *Lexer) *BreakStat {
	lexer.NextTokenOfKind(TOKEN_KW_BREAK)
	return &BreakStat{lexer.Line()}
}

// ‘::’ Name ‘::’
func parseLabelStat(lexer *Lexer) *LabelStat {
	lexer.NextTokenOfKind(TOKEN_SEP_LABEL) // ::
	_, name := lexer.NextIdentifier()      // name
	lexer.NextTokenOfKind(TOKEN_SEP_LABEL) // ::
	return &LabelStat{name}
}

// goto Name
func parseGotoStat(lexer *Lexer) *GotoStat {
	lexer.NextTokenOfKind(TOKEN_KW_GOTO) // goto
	_, name := lexer.NextIdentifier()    // name
	return &GotoStat{name}
}

// do block end
func parseDoStat(lexer *Lexer) *DoStat {
	lexer.NextTokenOfKind(TOKEN_KW_DO)  // do
	block := parseBlock(lexer)          // block
	lexer.NextTokenOfKind(TOKEN_KW_END) // end
	return &DoStat{block}
}

// while exp do block end
func parseWhileStat(lexer *Lexer) *WhileStat {
	lexer.NextTokenOfKind(TOKEN_KW_WHILE) // while
	exp := parseExp(lexer)                // exp
	lexer.NextTokenOfKind(TOKEN_KW_DO)    // do
	block := parseBlock(lexer)            // block
	lexer.NextTokenOfKind(TOKEN_KW_END)   // end
	return &WhileStat{exp, block}
}

// repeat block until exp
func parseRepeatStat(lexer *Lexer) *RepeatStat {
	lexer.NextTokenOfKind(TOKEN_KW_REPEAT) // repeat
	block := parseBlock(lexer)             // block
	lexer.NextTokenOfKind(TOKEN_KW_UNTIL)  // until
	exp := parseExp(lexer)                 // exp
	return &RepeatStat{block, exp}
}

// if exp then block {elseif exp then block} [else block] end
func parseIfStat(lexer *Lexer) *IfStat {
	exps := make([]Exp, 0, 4)
	blocks := make([]*Block, 0, 4)

	lexer.NextTokenOfKind(TOKEN_KW_IF)         // if
	exps = append(exps, parseExp(lexer))       // exp
	lexer.NextTokenOfKind(TOKEN_KW_THEN)       // then
	blocks = append(blocks, parseBlock(lexer)) // block

	for lexer.LookAhead() == TOKEN_KW_ELSEIF {
		lexer.NextToken()                          // elseif
		exps = append(exps, parseExp(lexer))       // exp
		lexer.NextTokenOfKind(TOKEN_KW_THEN)       // then
		blocks = append(blocks, parseBlock(lexer)) // block
	}

	// else block => elseif true then block
	if lexer.LookAhead() == TOKEN_KW_ELSE {
		lexer.NextToken()                           // else
		exps = append(exps, &TrueExp{lexer.Line()}) //
		blocks = append(blocks, parseBlock(lexer))  // block
	}

	lexer.NextTokenOfKind(TOKEN_KW_END) // end
	return &IfStat{exps, blocks}
}

// for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
// for namelist in explist do block end
func parseForStat(lexer *Lexer) Stat {
	lineOfFor, _ := lexer.NextTokenOfKind(TOKEN_KW_FOR)
	_, name := lexer.NextIdentifier()
	if lexer.LookAhead() == TOKEN_OP_ASSIGN {
		return _finishForNumStat(lexer, lineOfFor, name)
	} else {
		return _finishForInStat(lexer, name)
	}
}

// for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
func _finishForNumStat(lexer *Lexer, lineOfFor int, varName string) *ForNumStat {
	lexer.NextTokenOfKind(TOKEN_OP_ASSIGN) // for name =
	initExp := parseExp(lexer)             // exp
	lexer.NextTokenOfKind(TOKEN_SEP_COMMA) // ,
	limitExp := parseExp(lexer)            // exp

	var stepExp Exp
	if lexer.LookAhead() == TOKEN_SEP_COMMA {
		lexer.NextToken()         // ,
		stepExp = parseExp(lexer) // exp
	} else {
		stepExp = &IntegerExp{lexer.Line(), 1}
	}

	lineOfDo, _ := lexer.NextTokenOfKind(TOKEN_KW_DO) // do
	block := parseBlock(lexer)                        // block
	lexer.NextTokenOfKind(TOKEN_KW_END)               // end

	return &ForNumStat{lineOfFor, lineOfDo,
		varName, initExp, limitExp, stepExp, block}
}

// for namelist in explist do block end
// namelist ::= Name {‘,’ Name}
// explist ::= exp {‘,’ exp}
func _finishForInStat(lexer *Lexer, name0 string) *ForInStat {
	nameList := _finishNameList(lexer, name0)         // for namelist
	lexer.NextTokenOfKind(TOKEN_KW_IN)                // in
	expList := parseExpList(lexer)                    // explist
	lineOfDo, _ := lexer.NextTokenOfKind(TOKEN_KW_DO) // do
	block := parseBlock(lexer)                        // block
	lexer.NextTokenOfKind(TOKEN_KW_END)               // end
	return &ForInStat{lineOfDo, nameList, expList, block}
}

// namelist ::= Name {‘,’ Name}
func _finishNameList(lexer *Lexer, name0 string) []string {
	names := []string{name0}
	for lexer.LookAhead() == TOKEN_SEP_COMMA {
		lexer.NextToken()                 // ,
		_, name := lexer.NextIdentifier() // Name
		names = append(names, name)
	}
	return names
}

// local function Name funcbody
// local namelist [‘=’ explist]
func parseLocalAssignOrFuncDefStat(lexer *Lexer) Stat {
	lexer.NextTokenOfKind(TOKEN_KW_LOCAL)
	if lexer.LookAhead() == TOKEN_KW_FUNCTION {
		return _finishLocalFuncDefStat(lexer)
	} else {
		return _finishLocalVarDeclStat(lexer)
	}
}

/*
http://www.lua.org/manual/5.3/manual.html#3.4.11

function f() end          =>  f = function() end
function t.a.b.c.f() end  =>  t.a.b.c.f = function() end
function t.a.b.c:f() end  =>  t.a.b.c.f = function(self) end
local function f() end    =>  local f; f = function() end

The statement `local function f () body end`
translates to `local f; f = function () body end`
not to `local f = function () body end`
(This only makes a difference when the body of the function
 contains references to f.)
*/
// local function Name funcbody
func _finishLocalFuncDefStat(lexer *Lexer) *LocalFuncDefStat {
	lexer.NextTokenOfKind(TOKEN_KW_FUNCTION) // local function
	_, name := lexer.NextIdentifier()        // name
	fdExp := parseFuncDefExp(lexer)          // funcbody
	return &LocalFuncDefStat{name, fdExp}
}

// local namelist [‘=’ explist]
func _finishLocalVarDeclStat(lexer *Lexer) *LocalVarDeclStat {
	_, name0 := lexer.NextIdentifier()        // local Name
	nameList := _finishNameList(lexer, name0) // { , Name }
	var expList []Exp = nil
	if lexer.LookAhead() == TOKEN_OP_ASSIGN {
		lexer.NextToken()             // ==
		expList = parseExpList(lexer) // explist
	}
	lastLine := lexer.Line()
	return &LocalVarDeclStat{lastLine, nameList, expList}
}

// varlist ‘=’ explist
// functioncall
func parseAssignOrFuncCallStat(lexer *Lexer) Stat {
	prefixExp := parsePrefixExp(lexer)
	if fc, ok := prefixExp.(*FuncCallExp); ok {
		return fc
	} else {
		return parseAssignStat(lexer, prefixExp)
	}
}

// varlist ‘=’ explist |
func parseAssignStat(lexer *Lexer, var0 Exp) *AssignStat {
	varList := _finishVarList(lexer, var0) // varlist
	lexer.NextTokenOfKind(TOKEN_OP_ASSIGN) // =
	expList := parseExpList(lexer)         // explist
	lastLine := lexer.Line()
	return &AssignStat{lastLine, varList, expList}
}

// varlist ::= var {‘,’ var}
func _finishVarList(lexer *Lexer, var0 Exp) []Exp {
	vars := []Exp{_checkVar(lexer, var0)}      // var
	for lexer.LookAhead() == TOKEN_SEP_COMMA { // {
		lexer.NextToken()                          // ,
		exp := parsePrefixExp(lexer)               // var
		vars = append(vars, _checkVar(lexer, exp)) //
	} // }
	return vars
}

// var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
func _checkVar(lexer *Lexer, exp Exp) Exp {
	switch exp.(type) {
	case *NameExp, *TableAccessExp:
		return exp
	}
	lexer.NextTokenOfKind(-1) // trigger error
	panic("unreachable!")
}

// function funcname funcbody
// funcname ::= Name {‘.’ Name} [‘:’ Name]
// funcbody ::= ‘(’ [parlist] ‘)’ block end
// parlist ::= namelist [‘,’ ‘...’] | ‘...’
// namelist ::= Name {‘,’ Name}
func parseFuncDefStat(lexer *Lexer) *AssignStat {
	lexer.NextTokenOfKind(TOKEN_KW_FUNCTION) // function
	fnExp, hasColon := _parseFuncName(lexer) // funcname
	fdExp := parseFuncDefExp(lexer)          // funcbody
	if hasColon {                            // insert self
		fdExp.ParList = append(fdExp.ParList, "")
		copy(fdExp.ParList[1:], fdExp.ParList)
		fdExp.ParList[0] = "self"
	}

	return &AssignStat{
		LastLine: fdExp.Line,
		VarList:  []Exp{fnExp},
		ExpList:  []Exp{fdExp},
	}
}

// funcname ::= Name {‘.’ Name} [‘:’ Name]
func _parseFuncName(lexer *Lexer) (exp Exp, hasColon bool) {
	line, name := lexer.NextIdentifier()
	exp = &NameExp{line, name}

	for lexer.LookAhead() == TOKEN_SEP_DOT {
		lexer.NextToken()
		line, name := lexer.NextIdentifier()
		idx := &StringExp{line, name}
		exp = &TableAccessExp{line, exp, idx}
	}
	if lexer.LookAhead() == TOKEN_SEP_COLON {
		lexer.NextToken()
		line, name := lexer.NextIdentifier()
		idx := &StringExp{line, name}
		exp = &TableAccessExp{line, exp, idx}
		hasColon = true
	}

	return
}
