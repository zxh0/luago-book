package main

import "fmt"
import "io/ioutil"
import "os"
import . "luago/api"
import "luago/state"

func main() {
	if len(os.Args) > 1 {
		data, err := ioutil.ReadFile(os.Args[1])
		if err != nil {
			panic(err)
		}

		ls := state.New()
		ls.Register("print", print)
		ls.Register("getmetatable", getMetatable)
		ls.Register("setmetatable", setMetatable)
		ls.Register("next", next)
		ls.Register("pairs", pairs)
		ls.Register("ipairs", iPairs)
		ls.Register("error", error)
		ls.Register("pcall", pCall)
		ls.Load(data, os.Args[1], "b")
		ls.Call(0, 0)
	}
}

func print(ls LuaState) int {
	nArgs := ls.GetTop()
	for i := 1; i <= nArgs; i++ {
		if ls.IsBoolean(i) {
			fmt.Printf("%t", ls.ToBoolean(i))
		} else if ls.IsString(i) {
			fmt.Print(ls.ToString(i))
		} else {
			fmt.Print(ls.TypeName(ls.Type(i)))
		}
		if i < nArgs {
			fmt.Print("\t")
		}
	}
	fmt.Println()
	return 0
}

func getMetatable(ls LuaState) int {
	if !ls.GetMetatable(1) {
		ls.PushNil()
	}
	return 1
}

func setMetatable(ls LuaState) int {
	ls.SetMetatable(1)
	return 1
}

func next(ls LuaState) int {
	ls.SetTop(2) /* create a 2nd argument if there isn't one */
	if ls.Next(1) {
		return 2
	} else {
		ls.PushNil()
		return 1
	}
}

func pairs(ls LuaState) int {
	ls.PushGoFunction(next) /* will return generator, */
	ls.PushValue(1)         /* state, */
	ls.PushNil()
	return 3
}

func iPairs(ls LuaState) int {
	ls.PushGoFunction(_iPairsAux) /* iteration function */
	ls.PushValue(1)               /* state */
	ls.PushInteger(0)             /* initial value */
	return 3
}

func _iPairsAux(ls LuaState) int {
	i := ls.ToInteger(2) + 1
	ls.PushInteger(i)
	if ls.GetI(1, i) == LUA_TNIL {
		return 1
	} else {
		return 2
	}
}

func error(ls LuaState) int {
	return ls.Error()
}

func pCall(ls LuaState) int {
	nArgs := ls.GetTop() - 1
	status := ls.PCall(nArgs, -1, 0)
	ls.PushBoolean(status == LUA_OK)
	ls.Insert(1)
	return ls.GetTop()
}