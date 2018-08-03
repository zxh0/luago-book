package com.github.zxh0.luago.compiler.codegen;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.Stat;
import com.github.zxh0.luago.compiler.ast.exps.NameExp;
import com.github.zxh0.luago.compiler.ast.exps.TableAccessExp;
import com.github.zxh0.luago.compiler.ast.stats.*;

import java.util.Arrays;
import java.util.List;

import static com.github.zxh0.luago.compiler.codegen.BlockProcessor.processBlock;
import static com.github.zxh0.luago.compiler.codegen.ExpProcessor.*;

class StatProcessor {

    static void processStat(FuncInfo fi, Stat node) {
        if (node instanceof FuncCallStat) {
            processFuncCallStat(fi, (FuncCallStat) node);
        } else if (node instanceof BreakStat) {
            processBreakStat(fi, (BreakStat) node);
        } else if (node instanceof DoStat) {
            processDoStat(fi, (DoStat) node);
        } else if (node instanceof WhileStat) {
            processWhileStat(fi, (WhileStat) node);
        } else if (node instanceof RepeatStat) {
            processRepeatStat(fi, (RepeatStat) node);
        } else if (node instanceof IfStat) {
            processIfStat(fi, (IfStat) node);
        } else if (node instanceof ForNumStat) {
            processForNumStat(fi, (ForNumStat) node);
        } else if (node instanceof ForInStat) {
            processForInStat(fi, (ForInStat) node);
        } else if (node instanceof AssignStat) {
            processAssignStat(fi, (AssignStat) node);
        } else if (node instanceof LocalVarDeclStat) {
            processLocalVarDeclStat(fi, (LocalVarDeclStat) node);
        } else if (node instanceof LocalFuncDefStat) {
            processLocalFuncDefStat(fi, (LocalFuncDefStat) node);
        } else if (node instanceof LabelStat
                || node instanceof GotoStat) {
            throw new RuntimeException("label and goto statements are not supported!");
        }
    }

    private static void processLocalFuncDefStat(FuncInfo fi, LocalFuncDefStat node) {
        int r = fi.addLocVar(node.getName(), fi.pc()+2);
        processFuncDefExp(fi, node.getExp(), r);
    }

    private static void processFuncCallStat(FuncInfo fi, FuncCallStat node) {
        int r = fi.allocReg();
        processFuncCallExp(fi, node.getExp(), r, 0);
        fi.freeReg();
    }

    private static void processBreakStat(FuncInfo fi, BreakStat node) {
        int pc = fi.emitJmp(node.getLine(), 0, 0);
        fi.addBreakJmp(pc);
    }

    private static void processDoStat(FuncInfo fi, DoStat node) {
        fi.enterScope(false);
        processBlock(fi, node.getBlock());
        fi.closeOpenUpvals(node.getBlock().getLastLine());
        fi.exitScope(fi.pc() + 1);
    }

    /*
               ______________
              /  false? jmp  |
             /               |
    while exp do block end <-'
          ^           \
          |___________/
               jmp
    */
    private static void processWhileStat(FuncInfo fi, WhileStat node) {
        int pcBeforeExp = fi.pc();

        int oldRegs = fi.usedRegs;
        int a = expToOpArg(fi, node.getExp(), ARG_REG).arg;
        fi.usedRegs = oldRegs;

        int line = ExpHelper.lastLineOf(node.getExp());
        fi.emitTest(line, a, 0);
        int pcJmpToEnd = fi.emitJmp(line, 0, 0);

        fi.enterScope(true);
        processBlock(fi, node.getBlock());
        fi.closeOpenUpvals(node.getBlock().getLastLine());
        fi.emitJmp(node.getBlock().getLastLine(), 0, pcBeforeExp-fi.pc()-1);
        fi.exitScope(fi.pc());

        fi.fixSbx(pcJmpToEnd, fi.pc()-pcJmpToEnd);
    }

    /*
            ______________
           |  false? jmp  |
           V              /
    repeat block until exp
    */
    private static void processRepeatStat(FuncInfo fi, RepeatStat node) {
        fi.enterScope(true);

        int pcBeforeBlock = fi.pc();
        processBlock(fi, node.getBlock());

        int oldRegs = fi.usedRegs;
        int a = expToOpArg(fi, node.getExp(), ARG_REG).arg;
        fi.usedRegs = oldRegs;

        int line = ExpHelper.lastLineOf(node.getExp());
        fi.emitTest(line, a, 0);
        fi.emitJmp(line, fi.getJmpArgA(), pcBeforeBlock-fi.pc()-1);
        fi.closeOpenUpvals(line);

        fi.exitScope(fi.pc() + 1);
    }

    /*
             _________________       _________________       _____________
            / false? jmp      |     / false? jmp      |     / false? jmp  |
           /                  V    /                  V    /              V
    if exp1 then block1 elseif exp2 then block2 elseif true then block3 end <-.
                       \                       \                       \      |
                        \_______________________\_______________________\_____|
                        jmp                     jmp                     jmp
    */
    private static void processIfStat(FuncInfo fi, IfStat node) {
        int[] pcJmpToEnds = new int[node.getExps().size()];
        int pcJmpToNextExp = -1;

        for (int i = 0; i < node.getExps().size(); i++) {
            Exp exp = node.getExps().get(i);
            if (pcJmpToNextExp >= 0) {
                fi.fixSbx(pcJmpToNextExp, fi.pc()-pcJmpToNextExp);
            }

            int oldRegs = fi.usedRegs;
            int a = expToOpArg(fi, exp, ARG_REG).arg;
            fi.usedRegs = oldRegs;

            int line = ExpHelper.lastLineOf(exp);
            fi.emitTest(line, a, 0);
            pcJmpToNextExp = fi.emitJmp(line, 0, 0);

            Block block = node.getBlocks().get(i);
            fi.enterScope(false);
            processBlock(fi, block);
            fi.closeOpenUpvals(block.getLastLine());
            fi.exitScope(fi.pc() + 1);
            if (i < node.getExps().size()-1) {
                pcJmpToEnds[i] = fi.emitJmp(block.getLastLine(), 0, 0);
            } else {
                pcJmpToEnds[i] = pcJmpToNextExp;
            }
        }

        for (int pc : pcJmpToEnds) {
            fi.fixSbx(pc, fi.pc()-pc);
        }
    }

    private static void processForNumStat(FuncInfo fi, ForNumStat node) {
        String forIndexVar = "(for index)";
        String forLimitVar = "(for limit)";
        String forStepVar = "(for step)";

        fi.enterScope(true);

        LocalVarDeclStat lvdStat = new LocalVarDeclStat(0,
                Arrays.asList(forIndexVar, forLimitVar, forStepVar),
                Arrays.asList(node.getInitExp(), node.getLimitExp(), node.getStepExp()));
        processLocalVarDeclStat(fi, lvdStat);
        fi.addLocVar(node.getVarName(), fi.pc()+2);

        int a = fi.usedRegs - 4;
        int pcForPrep = fi.emitForPrep(node.getLineOfDo(), a, 0);
        processBlock(fi, node.getBlock());
        fi.closeOpenUpvals(node.getBlock().getLastLine());
        int pcForLoop = fi.emitForLoop(node.getLineOfFor(), a, 0);

        fi.fixSbx(pcForPrep, pcForLoop-pcForPrep-1);
        fi.fixSbx(pcForLoop, pcForPrep-pcForLoop);

        fi.exitScope(fi.pc());
        fi.fixEndPC(forIndexVar, 1);
        fi.fixEndPC(forLimitVar, 1);
        fi.fixEndPC(forStepVar, 1);
    }

    private static void processForInStat(FuncInfo fi, ForInStat node) {
        String forGeneratorVar = "(for generator)";
        String forStateVar = "(for state)";
        String forControlVar = "(for control)";

        fi.enterScope(true);

        LocalVarDeclStat lvdStat = new LocalVarDeclStat(0,
                Arrays.asList(forGeneratorVar, forStateVar, forControlVar),
                node.getExpList()
            );
        processLocalVarDeclStat(fi, lvdStat);
        for (String name : node.getNameList()) {
            fi.addLocVar(name, fi.pc()+2);
        }

        int pcJmpToTFC = fi.emitJmp(node.getLineOfDo(), 0, 0);
        processBlock(fi, node.getBlock());
        fi.closeOpenUpvals(node.getBlock().getLastLine());
        fi.fixSbx(pcJmpToTFC, fi.pc()-pcJmpToTFC);

        int line = ExpHelper.lineOf(node.getExpList().get(0));
        int rGenerator = fi.slotOfLocVar(forGeneratorVar);
        fi.emitTForCall(line, rGenerator, node.getNameList().size());
        fi.emitTForLoop(line, rGenerator+2, pcJmpToTFC-fi.pc()-1);

        fi.exitScope(fi.pc() - 1);
        fi.fixEndPC(forGeneratorVar, 2);
        fi.fixEndPC(forStateVar, 2);
        fi.fixEndPC(forControlVar, 2);
    }

    private static void processLocalVarDeclStat(FuncInfo fi, LocalVarDeclStat node) {
        List<Exp> exps = ExpHelper.removeTailNils(node.getExpList());
        int nExps = exps.size();
        int nNames = node.getNameList().size();

        int oldRegs = fi.usedRegs;
        if (nExps == nNames) {
            for(Exp exp : exps) {
                int a = fi.allocReg();
                processExp(fi, exp, a, 1);
            }
        } else if (nExps > nNames) {
            for (int i = 0; i < exps.size(); i++) {
                Exp exp = exps.get(i);
                int a = fi.allocReg();
                if (i == nExps-1 && ExpHelper.isVarargOrFuncCall(exp)) {
                    processExp(fi, exp, a, 0);
                } else {
                    processExp(fi, exp, a, 1);
                }
            }
        } else { // nNames > nExps
            boolean multRet = false;
            for (int i = 0; i < exps.size(); i++) {
                Exp exp = exps.get(i);
                int a = fi.allocReg();
                if (i == nExps-1 && ExpHelper.isVarargOrFuncCall(exp)) {
                    multRet = true;
                    int n = nNames - nExps + 1;
                    processExp(fi, exp, a, n);
                    fi.allocRegs(n - 1);
                } else {
                    processExp(fi, exp, a, 1);
                }
            }
            if (!multRet) {
                int n = nNames - nExps;
                int a = fi.allocRegs(n);
                fi.emitLoadNil(node.getLastLine(), a, n);
            }
        }

        fi.usedRegs = oldRegs;
        int startPC = fi.pc() + 1;
        for (String name : node.getNameList()) {
            fi.addLocVar(name, startPC);
        }
    }

    private static void processAssignStat(FuncInfo fi, AssignStat node) {
        List<Exp> exps = ExpHelper.removeTailNils(node.getExpList());
        int nExps = exps.size();
        int nVars = node.getVarList().size();

        int[] tRegs = new int[nVars];
        int[] kRegs = new int[nVars];
        int[] vRegs = new int[nVars];
        int oldRegs = fi.usedRegs;

        for (int i = 0; i < node.getVarList().size(); i++) {
            Exp exp = node.getVarList().get(i);
            if (exp instanceof TableAccessExp) {
                TableAccessExp taExp = (TableAccessExp) exp;
                tRegs[i] = fi.allocReg();
                processExp(fi, taExp.getPrefixExp(), tRegs[i], 1);
                kRegs[i] = fi.allocReg();
                processExp(fi, taExp.getKeyExp(), kRegs[i], 1);
            } else {
                String name = ((NameExp) exp).getName();
                if (fi.slotOfLocVar(name) < 0 && fi.indexOfUpval(name) < 0) {
                    // global var
                    kRegs[i] = -1;
                    if (fi.indexOfConstant(name) > 0xFF) {
                        kRegs[i] = fi.allocReg();
                    }
                }
            }
        }
        for (int i = 0; i < nVars; i++) {
            vRegs[i] = fi.usedRegs + i;
        }

        if (nExps >= nVars) {
            for (int i = 0; i < exps.size(); i++) {
                Exp exp = exps.get(i);
                int a = fi.allocReg();
                if (i >= nVars && i == nExps-1 && ExpHelper.isVarargOrFuncCall(exp)) {
                    processExp(fi, exp, a, 0);
                } else {
                    processExp(fi, exp, a, 1);
                }
            }
        } else { // nVars > nExps
            boolean multRet = false;
            for (int i = 0; i < exps.size(); i++) {
                Exp exp = exps.get(i);
                int a = fi.allocReg();
                if (i == nExps-1 && ExpHelper.isVarargOrFuncCall(exp)) {
                    multRet = true;
                    int n = nVars - nExps + 1;
                    processExp(fi, exp, a, n);
                    fi.allocRegs(n - 1);
                } else {
                    processExp(fi, exp, a, 1);
                }
            }
            if (!multRet) {
                int n = nVars - nExps;
                int a = fi.allocRegs(n);
                fi.emitLoadNil(node.getLastLine(), a, n);
            }
        }

        int lastLine = node.getLastLine();
        for (int i = 0; i < node.getVarList().size(); i++) {
            Exp exp = node.getVarList().get(i);
            if (! (exp instanceof NameExp)) {
                fi.emitSetTable(lastLine, tRegs[i], kRegs[i], vRegs[i]);
                continue;
            }

            NameExp nameExp = (NameExp) exp;
            String varName = nameExp.getName();
            int a = fi.slotOfLocVar(varName);
            if (a >= 0) {
                fi.emitMove(lastLine, a, vRegs[i]);
                continue;
            }

            int b = fi.indexOfUpval(varName);
            if (b >= 0) {
                fi.emitSetUpval(lastLine, vRegs[i], b);
                continue;
            }

            a = fi.slotOfLocVar("_ENV");
            if (a >= 0) {
                if (kRegs[i] < 0) {
                    b = 0x100 + fi.indexOfConstant(varName);
                    fi.emitSetTable(lastLine, a, b, vRegs[i]);
                } else {
                    fi.emitSetTable(lastLine, a, kRegs[i], vRegs[i]);
                }
                continue;
            }

            // global var
            a = fi.indexOfUpval("_ENV");
            if (kRegs[i] < 0) {
                b = 0x100 + fi.indexOfConstant(varName);
                fi.emitSetTabUp(lastLine, a, b, vRegs[i]);
            } else {
                fi.emitSetTabUp(lastLine, a, kRegs[i], vRegs[i]);
            }
        }

        // todo
        fi.usedRegs = oldRegs;
    }

}
