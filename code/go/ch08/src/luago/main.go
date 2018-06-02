package main

import "io/ioutil"
import "os"
import "luago/state"

func main() {
	if len(os.Args) > 1 {
		data, err := ioutil.ReadFile(os.Args[1])
		if err != nil {
			panic(err)
		}

		ls := state.New()
		ls.Load(data, os.Args[1], "b")
		ls.Call(0, 0)
	}
}
