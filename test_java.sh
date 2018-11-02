#!/bin/sh
set -ex
cd code/java

luac -o hw.luac   ../lua/ch02/hello_world.lua
luac -o sum.luac  ../lua/ch06/sum.lua
luac -o ch07.luac ../lua/ch07/test.lua
luac -o ch08.luac ../lua/ch08/test.lua
luac -o fact.luac ../lua/ch10/factorial.lua
luac -o fib.luac  ../lua/ch10/fibonacci.lua
luac -o ch10.luac ../lua/ch10/test.lua
luac -o vec2.luac ../lua/ch11/vector2.lua
luac -o ch12.luac ../lua/ch12/test.lua
luac -o ch13.luac ../lua/ch13/test.lua

sh gradlew ch02:run --args $PWD/hw.luac | grep -q main
sh gradlew ch03:run --args $PWD/hw.luac | grep -q LOADK
sh gradlew ch04:run | grep -q hello
sh gradlew ch05:run | grep -q "2.0"
sh gradlew ch06:run --args $PWD/sum.luac | grep -q 2550
sh gradlew ch07:run --args $PWD/ch07.luac | grep -q cBaBar3
sh gradlew ch08:run --args $PWD/ch08.luac | grep -q call
sh gradlew ch09:run --args $PWD/hw.luac | grep -q "Hello, World!"
sh gradlew ch10:run --args $PWD/fact.luac | grep -q 3628800
sh gradlew ch10:run --args $PWD/fib.luac | grep -q 987
sh gradlew ch10:run --args $PWD/ch10.luac | tr -d '\n' | grep -q 12132
sh gradlew ch11:run --args $PWD/vec2.luac | tr -d '\n' | grep -F -q "[1, 2][3, 4][2, 4][3, 6]5.0falsetrue[3, 6]"
sh gradlew ch12:run --args $PWD/ch12.luac | grep -q "a\t1"
sh gradlew ch13:run --args $PWD/ch13.luac | tr -d '\n\t' | grep -F -q "true2.0falseDIV BY ZERO !falsearithmetic error!"
sh gradlew ch14:run --args $PWD/../lua/ch02/hello_world.lua | grep EOF
sh gradlew ch15:run
sh gradlew ch16:run --args $PWD/../lua/ch02/hello_world.lua | grep -q print
sh gradlew ch17:run --args $PWD/../lua/ch02/hello_world.lua | grep -q "Hello, World!"
sh gradlew ch18:run --args $PWD/../lua/ch02/hello_world.lua | grep -q "Hello, World!"

echo OK