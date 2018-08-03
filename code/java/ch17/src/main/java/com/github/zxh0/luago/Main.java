package com.github.zxh0.luago;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.parser.Parser;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            byte[] data = Files.readAllBytes(Paths.get(args[0]));
            testParser(new String(data), args[0]);
        }
    }

    private static void testParser(String chunk, String chunkName) {
        Block block = Parser.parse(chunk, chunkName);
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(block);
        System.out.println(json);
    }

}
