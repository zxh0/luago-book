package com.github.zxh0.luago.compiler.codegen;

import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.exps.FuncDefExp;

public class CodeGen {

    public static Prototype genProto(Block chunk) {
        FuncDefExp fd = new FuncDefExp();
        fd.setLastLine(chunk.getLastLine());
        fd.setIsVararg(true);
        fd.setBlock(chunk);

        FuncInfo fi = new FuncInfo(null, fd);
        fi.addLocVar("_ENV", 0);
        ExpProcessor.processFuncDefExp(fi, fd, 0);
        return Fi2Proto.toProto(fi.subFuncs.get(0));
    }

}
