#!/bin/sh
set -ex

BOOK=$PWD
GO=$PWD/code/go
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

cd $GO/ch01
cd ../ch01;  GOPATH=$PWD go run luago 2>&1 | grep -q 'Hello, World!'
cd ../ch02;  GOPATH=$PWD go run luago $BOOK/hw.luac | grep -q main
cd ../ch03;  GOPATH=$PWD go run luago $BOOK/hw.luac | grep -q LOADK
cd ../ch04;  GOPATH=$PWD go run luago | grep -q hello
cd ../ch05;  GOPATH=$PWD go run luago | grep -q "2.0"
cd ../ch06;  GOPATH=$PWD go run luago $BOOK/sum.luac | grep -q 2550
cd ../ch07;  GOPATH=$PWD go run luago $BOOK/ch07.luac | grep -q cBaBar
cd ../ch08;  GOPATH=$PWD go run luago $BOOK/ch08.luac | grep -q call
cd ../ch09;  GOPATH=$PWD go run luago $BOOK/hw.luac | grep -q "Hello, World!"
cd ../ch10;  GOPATH=$PWD go run luago $BOOK/fact.luac | grep -q 3628800
cd ../ch10;  GOPATH=$PWD go run luago $BOOK/fib.luac | grep -q 987
cd ../ch10;  GOPATH=$PWD go run luago $BOOK/ch10.luac | tr -d '\n' | grep -q 12132
cd ../ch11;  GOPATH=$PWD go run luago $BOOK/vec2.luac | tr -d '\n' | grep -F -q "[1, 2][3, 4][2, 4][3, 6]5falsetrue[3, 6]"
cd ../ch12;  GOPATH=$PWD go run luago $BOOK/ch12.luac | grep -q "a	1"
cd ../ch13;  GOPATH=$PWD go run luago $BOOK/ch13.luac | tr -d '\n\t' | grep -F -q "true2falseDIV BY ZERO !falsearithmetic error!"
cd ../ch14;  GOPATH=$PWD go run luago $LUA/ch02/hello_world.lua | grep -q EOF
cd ../ch15;  GOPATH=$PWD go run luago
cd ../ch16;  GOPATH=$PWD go run luago $LUA/ch02/hello_world.lua 2>&1 | grep -q print
cd ../ch17;  GOPATH=$PWD go run luago $LUA/ch02/hello_world.lua | grep -q "Hello, World!"
cd ../ch17x; GOPATH=$PWD go run luago $LUA/ch02/hello_world.lua | grep -q "Hello, World!"
cd ../ch18;  GOPATH=$PWD go run luago $LUA/ch02/hello_world.lua | grep -q "Hello, World!"
cd ../ch19;  GOPATH=$PWD go run luago $LUA/ch19/examples.lua | grep -q "2018"
cd ../ch21;  GOPATH=$PWD go run luago $LUA/ch21/test.lua | tr -d '\n,' | grep -q "bcacbacabacbbacabc" 

cd ../ch20
cp $LUA/ch20/*.lua .
GOPATH=$PWD go run luago test.lua | tr -d '\n' | grep -q "foobar"
rm *.lua

cd ../ch21
GOPATH=$PWD go run luago $BOOK/bug21.luac | grep -q d


rm $BOOK/*.luac
echo OK