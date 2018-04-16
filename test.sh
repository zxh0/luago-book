#!/bin/sh
set -ex
cd code/go/ch01

# ch01 ~ ch05
cd ../ch01; export GOPATH=$PWD; go install luago
cd ../ch02; export GOPATH=$PWD; go install luago
cd ../ch03; export GOPATH=$PWD; go install luago
cd ../ch04; export GOPATH=$PWD; go install luago
cd ../ch05; export GOPATH=$PWD; go install luago

# ch06
cd ../ch06; export GOPATH=$PWD; go install luago
luac ../../lua/ch06/sum.lua
./bin/luago luac.out | grep -q 2550

# ch07
cd ../ch07; export GOPATH=$PWD; go install luago
luac ../../lua/ch07/test.lua
./bin/luago luac.out | grep -q cBaBar

# ch08
cd ../ch08; export GOPATH=$PWD; go install luago
luac ../../lua/ch08/test.lua
./bin/luago luac.out

# ch09
cd ../ch09; export GOPATH=$PWD; go install luago
luac ../../lua/ch02/hello_world.lua
./bin/luago luac.out | grep -q "Hello, World!"

# ch10
cd ../ch10; export GOPATH=$PWD; go install luago
luac ../../lua/ch10/factorial.lua
./bin/luago luac.out | grep -q 3628800
luac ../../lua/ch10/fibonacci.lua
./bin/luago luac.out | grep -q 987
luac ../../lua/ch10/test.lua
./bin/luago luac.out | tr -d '\n' | grep -q 12132

# ch11
cd ../ch11; export GOPATH=$PWD; go install luago
luac ../../lua/ch11/vector2.lua
./bin/luago luac.out | tr -d '\n' | grep -F -q "[1, 2][3, 4][2, 4][3, 6]5falsetrue[3, 6]"

# ch12
cd ../ch12; export GOPATH=$PWD; go install luago
luac ../../lua/ch12/test.lua
./bin/luago luac.out

# ch13
cd ../ch13; export GOPATH=$PWD; go install luago
luac ../../lua/ch13/test.lua
./bin/luago luac.out | tr -d '\n\t' | grep -F -q "true2falseDIV BY ZERO !falsearithmetic error!"

# ch14 ~ ch17
cd ../ch14; export GOPATH=$PWD; go install luago
cd ../ch15; export GOPATH=$PWD; go install luago
cd ../ch16; export GOPATH=$PWD; go install luago
cd ../ch17; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua | grep -q "Hello, World!"

# ch17x
cd ../ch17x; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua | grep -q "Hello, World!"

# ch18
cd ../ch18; export GOPATH=$PWD; go install luago
./bin/luago ../../lua/ch02/hello_world.lua | grep -q "Hello, World!"

# ch19 ~ ch21
cd ../ch19; export GOPATH=$PWD; go install luago
cd ../ch20; export GOPATH=$PWD; go install luago
cd ../ch21; export GOPATH=$PWD; go install luago

echo OK