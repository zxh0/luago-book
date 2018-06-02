package main

import "fmt"
import . "luago/api"
import _ "luago/binchunk"
import "luago/state"

func main() {
	ls := state.New()
	ls.PushInteger(1)
	ls.PushString("2.0")
	ls.PushString("3.0")
	ls.PushNumber(4.0)
	printStack(ls)

	ls.Arith(LUA_OPADD)
	printStack(ls)
	ls.Arith(LUA_OPBNOT)
	printStack(ls)
	ls.Len(2)
	printStack(ls)
	ls.Concat(3)
	printStack(ls)
	ls.PushBoolean(ls.Compare(1, 2, LUA_OPEQ))
	printStack(ls)
}

func printStack(ls LuaState) {
	top := ls.GetTop()
	for i := 1; i <= top; i++ {
		t := ls.Type(i)
		switch t {
		case LUA_TBOOLEAN:
			fmt.Printf("[%t]", ls.ToBoolean(i))
		case LUA_TNUMBER:
			fmt.Printf("[%g]", ls.ToNumber(i))
		case LUA_TSTRING:
			fmt.Printf("[%q]", ls.ToString(i))
		default: // other values
			fmt.Printf("[%s]", ls.TypeName(t))
		}
	}
	fmt.Println()
}
