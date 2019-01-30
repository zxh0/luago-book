#!/bin/sh
set -ex

BOOK=$PWD
RUST=$PWD/code/rust
LUA=$PWD/code/lua

luac -o hw.luac   $LUA/ch02/hello_world.lua
luac -o sum.luac  $LUA/ch06/sum.lua
luac -o ch07.luac $LUA/ch07/test.lua
luac -o ch08.luac $LUA/ch08/test.lua
# luac -o fact.luac $LUA/ch10/factorial.lua
# luac -o fib.luac  $LUA/ch10/fibonacci.lua
# luac -o ch10.luac $LUA/ch10/test.lua
# luac -o vec2.luac $LUA/ch11/vector2.lua
# luac -o ch12.luac $LUA/ch12/test.lua
# luac -o ch13.luac $LUA/ch13/test.lua

cd $RUST/ch01
cd ../ch01; cargo run 2>&1 | grep -q 'Hello, world!'
cd ../ch02; cargo run $BOOK/hw.luac | grep -q main
cd ../ch03; cargo run $BOOK/hw.luac | grep -q LOADK
cd ../ch04; cargo run | grep -q hello
cd ../ch05; cargo run | grep -q "2.0"
cd ../ch06; cargo run $BOOK/sum.luac | grep -q 2550
cd ../ch07; cargo run $BOOK/ch07.luac | grep -q cBaBar
cd ../ch08; cargo run $BOOK/ch08.luac | grep -q call
cd ../ch09; cargo run $BOOK/hw.luac | grep -q "Hello, World!"

rm $BOOK/*.luac
echo OK