#!/bin/sh
set -ex

BOOK=$PWD
GO=$PWD/code/go_mod
LUA=$PWD/code/lua

luac -o hw.luac   $LUA/ch02/hello_world.lua
luac -o sum.luac  $LUA/ch06/sum.lua
luac -o ch07.luac $LUA/ch07/test.lua
luac -o ch08.luac $LUA/ch08/test.lua
luac -o fact.luac $LUA/ch10/factorial.lua
luac -o fib.luac  $LUA/ch10/fibonacci.lua
luac -o ch10.luac $LUA/ch10/test.lua
luac -o vec2.luac $LUA/ch11/vector2.lua
luac -o ch12.luac $LUA/ch12/test.lua
luac -o ch13.luac $LUA/ch13/test.lua
luac -o bug21.luac $LUA/bugs/table_bug.lua

cd $GO/ch01/luago;  go run luago 2>&1 | grep -q 'Hello, World!'
cd $GO/ch02/luago;  go run luago $BOOK/hw.luac | grep -q main
cd $GO/ch03/luago;  go run luago $BOOK/hw.luac | grep -q LOADK
cd $GO/ch04/luago;  go run luago | grep -q hello
cd $GO/ch05/luago;  go run luago | grep -q "2.0"
cd $GO/ch06/luago;  go run luago $BOOK/sum.luac | grep -q 2550
cd $GO/ch07/luago;  go run luago $BOOK/ch07.luac | grep -q cBaBar
cd $GO/ch08/luago;  go run luago $BOOK/ch08.luac | grep -q call
cd $GO/ch09/luago;  go run luago $BOOK/hw.luac | grep -q "Hello, World!"
cd $GO/ch10/luago;  go run luago $BOOK/fact.luac | grep -q 3628800
cd $GO/ch10/luago;  go run luago $BOOK/fib.luac | grep -q 987
cd $GO/ch10/luago;  go run luago $BOOK/ch10.luac | tr -d '\n' | grep -q 12132
cd $GO/ch11/luago;  go run luago $BOOK/vec2.luac | tr -d '\n' | grep -F -q "[1, 2][3, 4][2, 4][3, 6]5falsetrue[3, 6]"
cd $GO/ch12/luago;  go run luago $BOOK/ch12.luac | grep -q "a	1"
cd $GO/ch13/luago;  go run luago $BOOK/ch13.luac | tr -d '\n\t' | grep -F -q "true2falseDIV BY ZERO !falsearithmetic error!"
cd $GO/ch14/luago;  go run luago $LUA/ch02/hello_world.lua | grep -q EOF
cd $GO/ch15/luago;  go run luago
cd $GO/ch16/luago;  go run luago $LUA/ch02/hello_world.lua 2>&1 | grep -q print
cd $GO/ch17/luago;  go run luago $LUA/ch02/hello_world.lua | grep -q "Hello, World!"
cd $GO/ch17x/luago; go run luago $LUA/ch02/hello_world.lua | grep -q "Hello, World!"
cd $GO/ch18/luago;  go run luago $LUA/ch02/hello_world.lua | grep -q "Hello, World!"
cd $GO/ch19/luago;  go run luago $LUA/ch19/examples.lua | grep -q "2018"
cd $GO/ch21/luago;  go run luago $LUA/ch21/test.lua | tr -d '\n,' | grep -q "bcacbacabacbbacabc" 

cd $GO/ch20/luago
cp $LUA/ch20/*.lua .
go run luago test.lua | tr -d '\n' | grep -q "foobar"
rm *.lua

cd $GO/ch21/luago
go run luago $BOOK/bug21.luac | grep -q d

rm $BOOK/*.luac
echo OK