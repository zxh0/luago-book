package main

import "encoding/json"
import "io/ioutil"
import "os"
import "luago/compiler/parser"

func main() {
	if len(os.Args) > 1 {
		data, err := ioutil.ReadFile(os.Args[1])
		if err != nil {
			panic(err)
		}

		testParser(os.Args[1], string(data))
	}
}

func testParser(source, chunk string) {
	ast := parser.Parse(source, chunk)
	b, err := json.Marshal(ast)
	if err != nil {
		panic(err)
	}
	println(string(b))
}
