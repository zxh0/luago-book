package com.github.zxh0.luago.compiler;

import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.codegen.CodeGen;
import com.github.zxh0.luago.compiler.parser.Parser;

public class Compiler {

    public static Prototype compile(String chunk, String chunkName) {
        Block ast = Parser.parse(chunk, chunkName);
        Prototype proto = CodeGen.genProto(ast);
        setSource(proto, chunkName);
        return proto;
    }

    private static void setSource(Prototype proto, String chunkName) {
        proto.setSource(chunkName);
        for (Prototype subProto : proto.getProtos()) {
            setSource(subProto, chunkName);
        }
    }

}
