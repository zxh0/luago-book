package main

import "os"
import "luago/state"

func main() {
	if len(os.Args) > 1 {
		ls := state.New()
		ls.OpenLibs()
		ls.DoFile(os.Args[1])
	}
}
