package main

import "os"
import "luago/state"

func main() {
	if len(os.Args) > 1 {
		ls := state.New()
		ls.OpenLibs()
		ls.LoadFile(os.Args[1])
		ls.Call(0, -1)
	}
}
