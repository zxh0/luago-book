package codegen

import . "luago/compiler/ast"
import . "luago/compiler/lexer"
import . "luago/vm"

var arithAndBitwiseBinops = map[int]int{
	TOKEN_OP_ADD:  OP_ADD,
	TOKEN_OP_SUB:  OP_SUB,
	TOKEN_OP_MUL:  OP_MUL,
	TOKEN_OP_MOD:  OP_MOD,
	TOKEN_OP_POW:  OP_POW,
	TOKEN_OP_DIV:  OP_DIV,
	TOKEN_OP_IDIV: OP_IDIV,
	TOKEN_OP_BAND: OP_BAND,
	TOKEN_OP_BOR:  OP_BOR,
	TOKEN_OP_BXOR: OP_BXOR,
	TOKEN_OP_SHL:  OP_SHL,
	TOKEN_OP_SHR:  OP_SHR,
}

type upvalInfo struct {
	locVarSlot int
	upvalIndex int
	index      int
}

type locVarInfo struct {
	prev     *locVarInfo
	name     string
	scopeLv  int
	slot     int
	captured bool
}

type funcInfo struct {
	parent    *funcInfo
	subFuncs  []*funcInfo
	usedRegs  int
	maxRegs   int
	scopeLv   int
	locVars   []*locVarInfo
	locNames  map[string]*locVarInfo
	upvalues  map[string]upvalInfo
	constants map[interface{}]int
	breaks    [][]int
	insts     []uint32
	numParams int
	isVararg  bool
}

func newFuncInfo(parent *funcInfo, fd *FuncDefExp) *funcInfo {
	return &funcInfo{
		parent:    parent,
		subFuncs:  []*funcInfo{},
		locVars:   make([]*locVarInfo, 0, 8),
		locNames:  map[string]*locVarInfo{},
		upvalues:  map[string]upvalInfo{},
		constants: map[interface{}]int{},
		breaks:    make([][]int, 1),
		insts:     make([]uint32, 0, 8),
		numParams: len(fd.ParList),
		isVararg:  fd.IsVararg,
	}
}

/* constants */

func (self *funcInfo) indexOfConstant(k interface{}) int {
	if idx, found := self.constants[k]; found {
		return idx
	}

	idx := len(self.constants)
	self.constants[k] = idx
	return idx
}

/* registers */

func (self *funcInfo) allocReg() int {
	self.usedRegs++
	if self.usedRegs >= 255 {
		panic("function or expression needs too many registers")
	}
	if self.usedRegs > self.maxRegs {
		self.maxRegs = self.usedRegs
	}
	return self.usedRegs - 1
}

func (self *funcInfo) freeReg() {
	if self.usedRegs <= 0 {
		panic("usedRegs <= 0 !")
	}
	self.usedRegs--
}

func (self *funcInfo) allocRegs(n int) int {
	if n <= 0 {
		panic("n <= 0 !")
	}
	for i := 0; i < n; i++ {
		self.allocReg()
	}
	return self.usedRegs - n
}

func (self *funcInfo) freeRegs(n int) {
	if n < 0 {
		panic("n < 0 !")
	}
	for i := 0; i < n; i++ {
		self.freeReg()
	}
}

/* lexical scope */

func (self *funcInfo) enterScope(breakable bool) {
	self.scopeLv++
	if breakable {
		self.breaks = append(self.breaks, []int{})
	} else {
		self.breaks = append(self.breaks, nil)
	}
}

func (self *funcInfo) exitScope() {
	pendingBreakJmps := self.breaks[len(self.breaks)-1]
	self.breaks = self.breaks[:len(self.breaks)-1]

	a := self.getJmpArgA()
	for _, pc := range pendingBreakJmps {
		sBx := self.pc() - pc
		i := (sBx+MAXARG_sBx)<<14 | a<<6 | OP_JMP
		self.insts[pc] = uint32(i)
	}

	self.scopeLv--
	for _, locVar := range self.locNames {
		if locVar.scopeLv > self.scopeLv { // out of scope
			self.removeLocVar(locVar)
		}
	}
}

func (self *funcInfo) removeLocVar(locVar *locVarInfo) {
	self.freeReg()
	if locVar.prev == nil {
		delete(self.locNames, locVar.name)
	} else if locVar.prev.scopeLv == locVar.scopeLv {
		self.removeLocVar(locVar.prev)
	} else {
		self.locNames[locVar.name] = locVar.prev
	}
}

func (self *funcInfo) addLocVar(name string) int {
	newVar := &locVarInfo{
		name:    name,
		prev:    self.locNames[name],
		scopeLv: self.scopeLv,
		slot:    self.allocReg(),
	}

	self.locVars = append(self.locVars, newVar)
	self.locNames[name] = newVar

	return newVar.slot
}

func (self *funcInfo) slotOfLocVar(name string) int {
	if locVar, found := self.locNames[name]; found {
		return locVar.slot
	}
	return -1
}

func (self *funcInfo) addBreakJmp(pc int) {
	for i := self.scopeLv; i >= 0; i-- {
		if self.breaks[i] != nil { // breakable
			self.breaks[i] = append(self.breaks[i], pc)
			return
		}
	}

	panic("<break> at line ? not inside a loop!")
}

/* upvalues */

func (self *funcInfo) indexOfUpval(name string) int {
	if upval, ok := self.upvalues[name]; ok {
		return upval.index
	}
	if self.parent != nil {
		if locVar, found := self.parent.locNames[name]; found {
			idx := len(self.upvalues)
			self.upvalues[name] = upvalInfo{locVar.slot, -1, idx}
			locVar.captured = true
			return idx
		}
		if uvIdx := self.parent.indexOfUpval(name); uvIdx >= 0 {
			idx := len(self.upvalues)
			self.upvalues[name] = upvalInfo{-1, uvIdx, idx}
			return idx
		}
	}
	return -1
}

func (self *funcInfo) closeOpenUpvals() {
	a := self.getJmpArgA()
	if a > 0 {
		self.emitJmp(a, 0)
	}
}

func (self *funcInfo) getJmpArgA() int {
	hasCapturedLocVars := false
	minSlotOfLocVars := self.maxRegs
	for _, locVar := range self.locNames {
		if locVar.scopeLv == self.scopeLv {
			for v := locVar; v != nil && v.scopeLv == self.scopeLv; v = v.prev {
				if v.captured {
					hasCapturedLocVars = true
				}
				if v.slot < minSlotOfLocVars && v.name[0] != '(' {
					minSlotOfLocVars = v.slot
				}
			}
		}
	}
	if hasCapturedLocVars {
		return minSlotOfLocVars + 1
	} else {
		return 0
	}
}

/* code */

func (self *funcInfo) pc() int {
	return len(self.insts) - 1
}

func (self *funcInfo) fixSbx(pc, sBx int) {
	i := self.insts[pc]
	i = i << 18 >> 18                  // clear sBx
	i = i | uint32(sBx+MAXARG_sBx)<<14 // reset sBx
	self.insts[pc] = i
}

func (self *funcInfo) emitABC(opcode, a, b, c int) {
	i := b<<23 | c<<14 | a<<6 | opcode
	self.insts = append(self.insts, uint32(i))
}

func (self *funcInfo) emitABx(opcode, a, bx int) {
	i := bx<<14 | a<<6 | opcode
	self.insts = append(self.insts, uint32(i))
}

func (self *funcInfo) emitAsBx(opcode, a, b int) {
	i := (b+MAXARG_sBx)<<14 | a<<6 | opcode
	self.insts = append(self.insts, uint32(i))
}

func (self *funcInfo) emitAx(opcode, ax int) {
	i := ax<<6 | opcode
	self.insts = append(self.insts, uint32(i))
}

// r[a] = r[b]
func (self *funcInfo) emitMove(a, b int) {
	self.emitABC(OP_MOVE, a, b, 0)
}

// r[a], r[a+1], ..., r[a+b] = nil
func (self *funcInfo) emitLoadNil(a, n int) {
	self.emitABC(OP_LOADNIL, a, n-1, 0)
}

// r[a] = (bool)b; if (c) pc++
func (self *funcInfo) emitLoadBool(a, b, c int) {
	self.emitABC(OP_LOADBOOL, a, b, c)
}

// r[a] = kst[bx]
func (self *funcInfo) emitLoadK(a int, k interface{}) {
	idx := self.indexOfConstant(k)
	if idx < (1 << 18) {
		self.emitABx(OP_LOADK, a, idx)
	} else {
		self.emitABx(OP_LOADKX, a, 0)
		self.emitAx(OP_EXTRAARG, idx)
	}
}

// r[a], r[a+1], ..., r[a+b-2] = vararg
func (self *funcInfo) emitVararg(a, n int) {
	self.emitABC(OP_VARARG, a, n+1, 0)
}

// r[a] = emitClosure(proto[bx])
func (self *funcInfo) emitClosure(a, bx int) {
	self.emitABx(OP_CLOSURE, a, bx)
}

// r[a] = {}
func (self *funcInfo) emitNewTable(a, nArr, nRec int) {
	self.emitABC(OP_NEWTABLE,
		a, Int2fb(nArr), Int2fb(nRec))
}

// r[a][(c-1)*FPF+i] := r[a+i], 1 <= i <= b
func (self *funcInfo) emitSetList(a, b, c int) {
	self.emitABC(OP_SETLIST, a, b, c)
}

// r[a] := r[b][rk(c)]
func (self *funcInfo) emitGetTable(a, b, c int) {
	self.emitABC(OP_GETTABLE, a, b, c)
}

// r[a][rk(b)] = rk(c)
func (self *funcInfo) emitSetTable(a, b, c int) {
	self.emitABC(OP_SETTABLE, a, b, c)
}

// r[a] = upval[b]
func (self *funcInfo) emitGetUpval(a, b int) {
	self.emitABC(OP_GETUPVAL, a, b, 0)
}

// upval[b] = r[a]
func (self *funcInfo) emitSetUpval(a, b int) {
	self.emitABC(OP_SETUPVAL, a, b, 0)
}

// r[a] = upval[b][rk(c)]
func (self *funcInfo) emitGetTabUp(a, b, c int) {
	self.emitABC(OP_GETTABUP, a, b, c)
}

// upval[a][rk(b)] = rk(c)
func (self *funcInfo) emitSetTabUp(a, b, c int) {
	self.emitABC(OP_SETTABUP, a, b, c)
}

// r[a], ..., r[a+c-2] = r[a](r[a+1], ..., r[a+b-1])
func (self *funcInfo) emitCall(a, nArgs, nRet int) {
	self.emitABC(OP_CALL, a, nArgs+1, nRet+1)
}

// return r[a](r[a+1], ... ,r[a+b-1])
func (self *funcInfo) emitTailCall(a, nArgs int) {
	self.emitABC(OP_TAILCALL, a, nArgs+1, 0)
}

// return r[a], ... ,r[a+b-2]
func (self *funcInfo) emitReturn(a, n int) {
	self.emitABC(OP_RETURN, a, n+1, 0)
}

// r[a+1] := r[b]; r[a] := r[b][rk(c)]
func (self *funcInfo) emitSelf(a, b, c int) {
	self.emitABC(OP_SELF, a, b, c)
}

// pc+=sBx; if (a) close all upvalues >= r[a - 1]
func (self *funcInfo) emitJmp(a, sBx int) int {
	self.emitAsBx(OP_JMP, a, sBx)
	return len(self.insts) - 1
}

// if not (r[a] <=> c) then pc++
func (self *funcInfo) emitTest(a, c int) {
	self.emitABC(OP_TEST, a, 0, c)
}

// if (r[b] <=> c) then r[a] := r[b] else pc++
func (self *funcInfo) emitTestSet(a, b, c int) {
	self.emitABC(OP_TESTSET, a, b, c)
}

func (self *funcInfo) emitForPrep(a, sBx int) int {
	self.emitAsBx(OP_FORPREP, a, sBx)
	return len(self.insts) - 1
}

func (self *funcInfo) emitForLoop(a, sBx int) int {
	self.emitAsBx(OP_FORLOOP, a, sBx)
	return len(self.insts) - 1
}

func (self *funcInfo) emitTForCall(a, c int) {
	self.emitABC(OP_TFORCALL, a, 0, c)
}

func (self *funcInfo) emitTForLoop(a, sBx int) {
	self.emitAsBx(OP_TFORLOOP, a, sBx)
}

// r[a] = op r[b]
func (self *funcInfo) emitUnaryOp(op, a, b int) {
	switch op {
	case TOKEN_OP_NOT:
		self.emitABC(OP_NOT, a, b, 0)
	case TOKEN_OP_BNOT:
		self.emitABC(OP_BNOT, a, b, 0)
	case TOKEN_OP_LEN:
		self.emitABC(OP_LEN, a, b, 0)
	case TOKEN_OP_UNM:
		self.emitABC(OP_UNM, a, b, 0)
	}
}

// r[a] = rk[b] op rk[c]
// arith & bitwise & relational
func (self *funcInfo) emitBinaryOp(op, a, b, c int) {
	if opcode, found := arithAndBitwiseBinops[op]; found {
		self.emitABC(opcode, a, b, c)
	} else {
		switch op {
		case TOKEN_OP_EQ:
			self.emitABC(OP_EQ, 1, b, c)
		case TOKEN_OP_NE:
			self.emitABC(OP_EQ, 0, b, c)
		case TOKEN_OP_LT:
			self.emitABC(OP_LT, 1, b, c)
		case TOKEN_OP_GT:
			self.emitABC(OP_LT, 1, c, b)
		case TOKEN_OP_LE:
			self.emitABC(OP_LE, 1, b, c)
		case TOKEN_OP_GE:
			self.emitABC(OP_LE, 1, c, b)
		}
		self.emitJmp(0, 1)
		self.emitLoadBool(a, 0, 1)
		self.emitLoadBool(a, 1, 0)
	}
}
