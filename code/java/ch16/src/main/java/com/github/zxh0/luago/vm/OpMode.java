package com.github.zxh0.luago.vm;

public enum OpMode {

    iABC , // [  B:9  ][  C:9  ][ A:8  ][OP:6]
    iABx , // [      Bx:18     ][ A:8  ][OP:6]
    iAsBx, // [     sBx:18     ][ A:8  ][OP:6]
    iAx  , // [           Ax:26        ][OP:6]
    ;

}
