package com.github.zxh0.luago;

import com.github.zxh0.luago.api.LuaState;
import com.github.zxh0.luago.api.LuaType;
import com.github.zxh0.luago.api.LuaVM;
import com.github.zxh0.luago.binchunk.BinaryChunk;
import com.github.zxh0.luago.binchunk.Prototype;
import com.github.zxh0.luago.state.LuaStateImpl;
import com.github.zxh0.luago.vm.Instruction;
import com.github.zxh0.luago.vm.OpCode;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            byte[] data = Files.readAllBytes(Paths.get(args[0]));
            Prototype proto = BinaryChunk.undump(data);
            luaMain(proto);
        }
    }

    private static void luaMain(Prototype proto) {
        LuaVM vm = new LuaStateImpl(proto);
        vm.setTop(proto.getMaxStackSize());
        for (;;) {
            int pc = vm.getPC();
            int i = vm.fetch();
            OpCode opCode = Instruction.getOpCode(i);
            if (opCode != OpCode.RETURN) {
                opCode.getAction().execute(i, vm);

                System.out.printf("[%02d] %-8s ", pc+1, opCode.name());
                printStack(vm);
            } else {
                break;
            }
        }
    }

    private static void printStack(LuaState ls) {
        int top = ls.getTop();
        for (int i = 1; i <= top; i++) {
            LuaType t = ls.type(i);
            switch (t) {
                case LUA_TBOOLEAN:
                    System.out.printf("[%b]", ls.toBoolean(i));
                    break;
                case LUA_TNUMBER:
                    if (ls.isInteger(i)) {
                        System.out.printf("[%d]", ls.toInteger(i));
                    } else {
                        System.out.printf("[%f]", ls.toNumber(i));
                    }
                    break;
                case LUA_TSTRING:
                    System.out.printf("[\"%s\"]", ls.toString(i));
                    break;
                default: // other values
                    System.out.printf("[%s]", ls.typeName(t));
                    break;
            }
        }
        System.out.println();
    }

}
