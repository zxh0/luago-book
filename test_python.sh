#!/bin/sh
set -ex

BOOK=$PWD
PYTHON=$PWD/code/python
LUA=$PWD/code/python/src/lua

cd $PYTHON/ch07/src;    python3 ./test_table.py                $LUA/table.luac              | grep -q cBaBar
cd $PYTHON/ch08/src;    python3 ./test_function_call.py        $LUA/function_call.luac      | grep -q call
cd $PYTHON/ch09/src;    python3 ./test_py_func_call.py         $LUA/py_function_call.luac   | grep -q "hello world"
cd $PYTHON/ch10/src;    python3 ./test_closure_upvalue.py      $LUA/fib.luac                | grep -q "987"
cd $PYTHON/ch11/src;    python3 ./test_metatable_metamethod.py $LUA/vector.luac             | tr -d '\n' | grep -F -q "[1, 2][3, 4][2, 4][3, 6]5falsetrue[3, 6]"
cd $PYTHON/ch12/src;    python3 ./test_iterator.py             $LUA/iterator.luac           | grep -q "a 1"
cd $PYTHON/ch13/src;    python3 ./test_exception.py            $LUA/exception.luac          | tr -d '\n\t' | grep -F -q "true2.0falseDIV BY 0 !falsearithmetic error"
cd $PYTHON/ch14/src;    python3 ./test_lexer.py                $LUA/hello.lua               | grep -q EOF
cd $PYTHON/ch15_16/src; python3 ./test_parser.py               $LUA/hello.lua               | grep -q "print"
cd $PYTHON/ch17/src;    python3 ./test_code_gen.py             $LUA/hello.lua               | grep -q "Hello World"
cd $BOOK

echo OK
