#!/bin/sh
set -ex
cd code/go/ch01

cd ../ch01; export GOPATH=$PWD; go install luago
./bin/luago 2>&1 | grep -q 'Hello, World!'

cd ../ch02; export GOPATH=$PWD; go install luago
luac ../../lua/ch02/hello_world.lua
./bin/luago luac.out | grep -q main

cd ../ch03; export GOPATH=$PWD; go install luago
luac ../../lua/ch02/hello_world.lua
./bin/luago luac.out | grep -q LOADK

cd ../ch04; export GOPATH=$PWD; go install luago
./bin/luago | grep -q hello

cd ../ch05; export GOPATH=$PWD; go install luago
bin/luago | grep -q "2.0"

cd ../ch06; export GOPATH=$PWD; go install luago
luac ../../lua/ch06/sum.lua
./bin/luago luac.out | grep -q 2550

cd ../ch07; export GOPATH=$PWD; go install luago
luac ../../lua/ch07/test.lua
./bin/luago luac.out | grep -q cBaBar

cd ../ch08; export GOPATH=$PWD; go install luago
luac ../../lua/ch08/test.lua
./bin/luago luac.out | grep -q call

cd ../ch09; export GOPATH=$PWD; go install luago
luac ../../lua/ch02/hello_world.lua
./bin/luago luac.out | grep -q "Hello, World!"

cd ../ch10; export GOPATH=$PWD; go install luago
luac ../../lua/ch10/factorial.lua
./bin/luago luac.out | grep -q 3628800
luac ../../lua/ch10/fibonacci.lua
./bin/luago luac.out | grep -q 987
luac ../../lua/ch10/test.lua
./bin/luago luac.out | tr -d '\n' | grep -q 12132

cd ../ch11; export GOPATH=$PWD; go install luago
luac ../../lua/ch11/vector2.lua
./bin/luago luac.out | tr -d '\n' | grep -F -q "[1, 2][3, 4][2, 4][3, 6]5falsetrue[3, 6]"

cd ../ch12; export GOPATH=$PWD; go install luago
luac ../../lua/ch12/test.lua
./bin/luago luac.out | grep -q "a	1"

cd ../ch13; export GOPATH=$PWD; go install luago
luac ../../lua/ch13/test.lua
./bin/luago luac.out | tr -d '\n\t' | grep -F -q "true2falseDIV BY ZERO !falsearithmetic error!"

cd ../ch14; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua | grep -q EOF

cd ../ch15; export GOPATH=$PWD; go install luago

cd ../ch16; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua 2>&1 | grep -q print

cd ../ch17; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua | grep -q "Hello, World!"

cd ../ch17x; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua | grep -q "Hello, World!"

cd ../ch18; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua | grep -q "Hello, World!"

cd ../ch19; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch19/examples.lua | grep -q "2018"

cd ../ch20; export GOPATH=$PWD; go install luago


cd ../ch21; export GOPATH=$PWD; go install luago

echo OK