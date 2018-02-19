package stdlib

import . "luago/api"

var coFuncs = map[string]GoFunction{
	"create":      coCreate,
	"resume":      coResume,
	"yield":       coYield,
	"status":      coStatus,
	"isyieldable": coYieldable,
	"running":     coRunning,
	"wrap":        coWrap,
}

func OpenCoroutineLib(ls LuaState) int {
	ls.NewLib(coFuncs)
	return 1
}

// coroutine.create (f)
// http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.create
// lua-5.3.4/src/lcorolib.c#luaB_cocreate()
func coCreate(ls LuaState) int {
	ls.CheckType(1, LUA_TFUNCTION)
	ls2 := ls.NewThread()
	ls.PushValue(1)  /* move function to top */
	ls.XMove(ls2, 1) /* move function from ls to ls2 */
	return 1
}

// coroutine.resume (co [, val1, ···])
// http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.resume
// lua-5.3.4/src/lcorolib.c#luaB_coresume()
func coResume(ls LuaState) int {
	co := ls.ToThread(1)
	ls.ArgCheck(co != nil, 1, "thread expected")

	if r := _auxResume(ls, co, ls.GetTop()-1); r < 0 {
		ls.PushBoolean(false)
		ls.Insert(-2)
		return 2 /* return false + error message */
	} else {
		ls.PushBoolean(true)
		ls.Insert(-(r + 1))
		return r + 1 /* return true + 'resume' returns */
	}
}

func _auxResume(ls, co LuaState, narg int) int {
	if !ls.CheckStack(narg) {
		ls.PushString("too many arguments to resume")
		return -1 /* error flag */
	}
	if co.Status() == LUA_OK && co.GetTop() == 0 {
		ls.PushString("cannot resume dead coroutine")
		return -1 /* error flag */
	}
	ls.XMove(co, narg)
	status := co.Resume(ls, narg)
	if status == LUA_OK || status == LUA_YIELD {
		nres := co.GetTop()
		if !ls.CheckStack(nres + 1) {
			co.Pop(nres) /* remove results anyway */
			ls.PushString("too many results to resume")
			return -1 /* error flag */
		}
		co.XMove(ls, nres) /* move yielded values */
		return nres
	} else {
		co.XMove(ls, 1) /* move error message */
		return -1       /* error flag */
	}
}

// coroutine.yield (···)
// http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.yield
// lua-5.3.4/src/lcorolib.c#luaB_yield()
func coYield(ls LuaState) int {
	return ls.Yield(ls.GetTop())
}

// coroutine.status (co)
// http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.status
// lua-5.3.4/src/lcorolib.c#luaB_costatus()
func coStatus(ls LuaState) int {
	co := ls.ToThread(1)
	ls.ArgCheck(co != nil, 1, "thread expected")
	if ls == co {
		ls.PushString("running")
	} else {
		switch co.Status() {
		case LUA_YIELD:
			ls.PushString("suspended")
		case LUA_OK:
			if co.GetStack() { /* does it have frames? */
				ls.PushString("normal") /* it is running */
			} else if co.GetTop() == 0 {
				ls.PushString("dead")
			} else {
				ls.PushString("suspended")
			}
		default: /* some error occurred */
			ls.PushString("dead")
		}
	}

	return 1
}

// coroutine.isyieldable ()
// http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.isyieldable
func coYieldable(ls LuaState) int {
	ls.PushBoolean(ls.IsYieldable())
	return 1
}

// coroutine.running ()
// http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.running
func coRunning(ls LuaState) int {
	isMain := ls.PushThread()
	ls.PushBoolean(isMain)
	return 2
}

// coroutine.wrap (f)
// http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.wrap
func coWrap(ls LuaState) int {
	panic("todo: coWrap!")
}
