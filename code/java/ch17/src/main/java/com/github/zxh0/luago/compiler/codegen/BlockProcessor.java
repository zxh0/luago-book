package com.github.zxh0.luago.compiler.codegen;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.Stat;
import com.github.zxh0.luago.compiler.ast.exps.FuncCallExp;
import com.github.zxh0.luago.compiler.ast.exps.NameExp;

import java.util.List;

import static com.github.zxh0.luago.compiler.codegen.ExpProcessor.processExp;
import static com.github.zxh0.luago.compiler.codegen.ExpProcessor.processTailCallExp;
import static com.github.zxh0.luago.compiler.codegen.StatProcessor.processStat;

class BlockProcessor {

    static void processBlock(FuncInfo fi, Block node) {
        for (Stat stat : node.getStats()) {
            processStat(fi, stat);
        }

        if (node.getRetExps() != null) {
            processRetStat(fi, node.getRetExps(), node.getLastLine());
        }
    }

    private static void processRetStat(FuncInfo fi, List<Exp> exps, int lastLine) {
        int nExps = exps.size();
        if (nExps == 0) {
            fi.emitReturn(lastLine, 0, 0);
            return;
        }

        if (nExps == 1) {
            if (exps.get(0) instanceof NameExp) {
                NameExp nameExp = (NameExp) exps.get(0);
                int r = fi.slotOfLocVar(nameExp.getName());
                if (r >= 0) {
                    fi.emitReturn(lastLine, r, 1);
                    return;
                }
            }
            if (exps.get(0) instanceof FuncCallExp) {
                FuncCallExp fcExp = (FuncCallExp) exps.get(0);
                int r = fi.allocReg();
                processTailCallExp(fi, fcExp, r);
                fi.freeReg();
                fi.emitReturn(lastLine, r, -1);
                return;
            }
        }

        boolean multRet = ExpHelper.isVarargOrFuncCall(exps.get(nExps-1));
        for (int i = 0; i < nExps; i++) {
            Exp exp = exps.get(i);
            int r = fi.allocReg();
            if (i == nExps-1 && multRet) {
                processExp(fi, exp, r, -1);
            } else {
                processExp(fi, exp, r, 1);
            }
        }
        fi.freeRegs(nExps);

        int a = fi.usedRegs; // correct?
        if (multRet) {
            fi.emitReturn(lastLine, a, -1);
        } else {
            fi.emitReturn(lastLine, a, nExps);
        }
    }

}
