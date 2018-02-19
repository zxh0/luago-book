package ast

/*
stat ::=  ‘;’ |
	 varlist ‘=’ explist |
	 functioncall |
	 label |
	 break |
	 goto Name |
	 do block end |
	 while exp do block end |
	 repeat block until exp |
	 if exp then block {elseif exp then block} [else block] end |
	 for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end |
	 for namelist in explist do block end |
	 function funcname funcbody |
	 local function Name funcbody |
	 local namelist [‘=’ explist]
*/
type Stat interface{}

type EmptyStat struct{}              // ‘;’
type BreakStat struct{ Line int }    // break
type LabelStat struct{ Name string } // ‘::’ Name ‘::’
type GotoStat struct{ Name string }  // goto Name
type DoStat struct{ Block *Block }   // do block end
type FuncCallStat = FuncCallExp      // functioncall

// if exp then block {elseif exp then block} [else block] end
type IfStat struct {
	Exps   []Exp
	Blocks []*Block
}

// while exp do block end
type WhileStat struct {
	Exp   Exp
	Block *Block
}

// repeat block until exp
type RepeatStat struct {
	Block *Block
	Exp   Exp
}

// for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
type ForNumStat struct {
	LineOfFor int
	LineOfDo  int
	VarName   string
	InitExp   Exp
	LimitExp  Exp
	StepExp   Exp
	Block     *Block
}

// for namelist in explist do block end
// namelist ::= Name {‘,’ Name}
// explist ::= exp {‘,’ exp}
type ForInStat struct {
	LineOfDo int
	NameList []string
	ExpList  []Exp
	Block    *Block
}

// varlist ‘=’ explist
// varlist ::= var {‘,’ var}
// var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
type AssignStat struct {
	LastLine int
	VarList  []Exp
	ExpList  []Exp
}

// local namelist [‘=’ explist]
// namelist ::= Name {‘,’ Name}
// explist ::= exp {‘,’ exp}
type LocalVarDeclStat struct {
	LastLine int
	NameList []string
	ExpList  []Exp
}

// local function Name funcbody
type LocalFuncDefStat struct {
	Name string
	Exp  *FuncDefExp
}
