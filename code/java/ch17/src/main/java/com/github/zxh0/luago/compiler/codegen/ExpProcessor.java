package com.github.zxh0.luago.compiler.codegen;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.exps.*;
import com.github.zxh0.luago.vm.OpCode;

import java.util.Collections;
import java.util.List;

import static com.github.zxh0.luago.compiler.codegen.BlockProcessor.processBlock;
import static com.github.zxh0.luago.compiler.lexer.TokenKind.TOKEN_OP_AND;
import static com.github.zxh0.luago.compiler.lexer.TokenKind.TOKEN_OP_OR;

class ExpProcessor {

    // kind of operands
    static final int ARG_CONST = 1; // const index
    static final int ARG_REG   = 2; // register index
    static final int ARG_UPVAL = 4; // upvalue index
    static final int ARG_RK    = ARG_REG | ARG_CONST;
    static final int ARG_RU    = ARG_REG | ARG_UPVAL;
  //static final int ARG_RUK   = ARG_REG | ARG_UPVAL | ARG_CONST;

    static class ArgAndKind {
        int arg;
        int kind;
    }


    static void processExp(FuncInfo fi, Exp node, int a, int n) {
        if (node instanceof NilExp) {
            fi.emitLoadNil(node.getLine(), a, n);
        } else if (node instanceof FalseExp) {
            fi.emitLoadBool(node.getLine(), a, 0, 0);
        } else if (node instanceof TrueExp) {
            fi.emitLoadBool(node.getLine(), a, 1, 0);
        } else if (node instanceof IntegerExp) {
            fi.emitLoadK(node.getLine(), a, ((IntegerExp) node).getVal());
        } else if (node instanceof FloatExp) {
            fi.emitLoadK(node.getLine(), a, ((FloatExp) node).getVal());
        } else if (node instanceof StringExp) {
            fi.emitLoadK(node.getLine(), a, ((StringExp) node).getStr());
        } else if (node instanceof ParensExp) {
            processExp(fi, ((ParensExp) node).getExp(), a, 1);
        } else if (node instanceof VarargExp) {
            processVarargExp(fi, (VarargExp) node, a, n);
        } else if (node instanceof FuncDefExp) {
            processFuncDefExp(fi, (FuncDefExp) node, a);
        } else if (node instanceof TableConstructorExp) {
            processTableConstructorExp(fi, (TableConstructorExp) node, a);
        } else if (node instanceof UnopExp) {
            processUnopExp(fi, (UnopExp) node, a);
        } else if (node instanceof BinopExp) {
            processBinopExp(fi, (BinopExp) node, a);
        } else if (node instanceof ConcatExp) {
            processConcatExp(fi, (ConcatExp) node, a);
        } else if (node instanceof NameExp) {
            processNameExp(fi, (NameExp) node, a);
        } else if (node instanceof TableAccessExp) {
            processTableAccessExp(fi, (TableAccessExp) node, a);
        } else if (node instanceof FuncCallExp) {
            processFuncCallExp(fi, (FuncCallExp) node, a, n);
        }
    }

    private static void processVarargExp(FuncInfo fi, VarargExp node, int a, int n) {
        if (!fi.isVararg) {
            throw new RuntimeException("cannot use '...' outside a vararg function");
        }
        fi.emitVararg(node.getLine(), a, n);
    }

    // f[a] = function(args) body end
    static void processFuncDefExp(FuncInfo fi, FuncDefExp node, int a) {
        FuncInfo subFI = new FuncInfo(fi, node);
        fi.subFuncs.add(subFI);

        if (node.getParList() != null) {
            for (String param : node.getParList()) {
                subFI.addLocVar(param, 0);
            }
        }

        processBlock(subFI, node.getBlock());
        subFI.exitScope(subFI.pc() + 2);
        subFI.emitReturn(node.getLastLine(), 0, 0);

        int bx = fi.subFuncs.size() - 1;
        fi.emitClosure(node.getLastLine(), a, bx);
    }

    private static void processTableConstructorExp(FuncInfo fi, TableConstructorExp node, int a) {
        int nArr = 0;
        for (Exp keyExp : node.getKeyExps()) {
            if (keyExp == null) {
                nArr++;
            }
        }
        int nExps = node.getKeyExps().size();
        boolean multRet = nExps > 0 &&
                ExpHelper.isVarargOrFuncCall(node.getValExps().get(nExps-1));

        fi.emitNewTable(node.getLine(), a, nArr, nExps-nArr);

        int arrIdx = 0;
        for (int i = 0; i < node.getKeyExps().size(); i++) {
            Exp keyExp = node.getKeyExps().get(i);
            Exp valExp = node.getValExps().get(i);

            if (keyExp == null) {
                arrIdx++;
                int tmp = fi.allocReg();
                if (i == nExps-1 && multRet) {
                    processExp(fi, valExp, tmp, -1);
                } else {
                    processExp(fi, valExp, tmp, 1);
                }

                if (arrIdx%50 == 0 || arrIdx == nArr) { // LFIELDS_PER_FLUSH
                    int n = arrIdx % 50;
                    if (n == 0) {
                        n = 50;
                    }
                    fi.freeRegs(n);
                    int line = ExpHelper.lastLineOf(valExp);
                    int c = (arrIdx-1)/50 + 1; // todo: c > 0xFF
                    if (i == nExps-1 && multRet) {
                        fi.emitSetList(line, a, 0, c);
                    } else {
                        fi.emitSetList(line, a, n, c);
                    }
                }

                continue;
            }

            int b = fi.allocReg();
            processExp(fi, keyExp, b, 1);
            int c = fi.allocReg();
            processExp(fi, valExp, c, 1);
            fi.freeRegs(2);

            int line = ExpHelper.lastLineOf(valExp);
            fi.emitSetTable(line, a, b, c);
        }
    }

    // r[a] = op exp
    private static void processUnopExp(FuncInfo fi, UnopExp node, int a) {
        int oldRegs = fi.usedRegs;
        int b = expToOpArg(fi, node.getExp(), ARG_REG).arg;
        fi.emitUnaryOp(node.getLine(), node.getOp(), a, b);
        fi.usedRegs = oldRegs;
    }

    // r[a] = exp1 op exp2
    private static void processBinopExp(FuncInfo fi, BinopExp node, int a) {
        if (node.getOp() == TOKEN_OP_AND || node.getOp() == TOKEN_OP_OR) {
            int oldRegs = fi.usedRegs;

            int b = expToOpArg(fi, node.getExp1(), ARG_REG).arg;
            fi.usedRegs = oldRegs;
            if (node.getOp() == TOKEN_OP_AND) {
                fi.emitTestSet(node.getLine(), a, b, 0);
            } else {
                fi.emitTestSet(node.getLine(), a, b, 1);
            }
            int pcOfJmp = fi.emitJmp(node.getLine(), 0, 0);

            b = expToOpArg(fi, node.getExp2(), ARG_REG).arg;
            fi.usedRegs = oldRegs;
            fi.emitMove(node.getLine(), a, b);
            fi.fixSbx(pcOfJmp, fi.pc()-pcOfJmp);
        } else {
            int oldRegs = fi.usedRegs;
            int b = expToOpArg(fi, node.getExp1(), ARG_RK).arg;
            int c = expToOpArg(fi, node.getExp2(), ARG_RK).arg;
            fi.emitBinaryOp(node.getLine(), node.getOp(), a, b, c);
            fi.usedRegs = oldRegs;
        }
    }

    // r[a] = exp1 .. exp2
    private static void processConcatExp(FuncInfo fi, ConcatExp node, int a) {
        for (Exp subExp : node.getExps()) {
            int a1 = fi.allocReg();
            processExp(fi, subExp, a1, 1);
        }

        int c = fi.usedRegs - 1;
        int b = c - node.getExps().size() + 1;
        fi.freeRegs(c - b + 1);
        fi.emitABC(node.getLine(), OpCode.CONCAT, a, b, c);
    }

    // r[a] = name
    private static void processNameExp(FuncInfo fi, NameExp node, int a) {
        int r = fi.slotOfLocVar(node.getName());
        if (r >= 0) {
            fi.emitMove(node.getLine(), a, r);
            return;
        }

        int idx = fi.indexOfUpval(node.getName());
        if (idx >= 0) {
            fi.emitGetUpval(node.getLine(), a, idx);
            return;
        }

        // x => _ENV['x']
        Exp prefixExp = new NameExp(node.getLine(), "_ENV");
        Exp keyExp = new StringExp(node.getLine(), node.getName());
        TableAccessExp taExp = new TableAccessExp(node.getLine(), prefixExp, keyExp);
        processTableAccessExp(fi, taExp, a);
    }

    // r[a] = prefix[key]
    private static void processTableAccessExp(FuncInfo fi, TableAccessExp node, int a) {
        int oldRegs = fi.usedRegs;
        ArgAndKind argAndKindB = expToOpArg(fi, node.getPrefixExp(), ARG_RU);
        int b = argAndKindB.arg;
        int c = expToOpArg(fi, node.getKeyExp(), ARG_RK).arg;
        fi.usedRegs = oldRegs;

        if (argAndKindB.kind == ARG_UPVAL) {
            fi.emitGetTabUp(node.getLastLine(), a, b, c);
        } else {
            fi.emitGetTable(node.getLastLine(), a, b, c);
        }
    }

    // r[a] = f(args)
    static void processFuncCallExp(FuncInfo fi, FuncCallExp node, int a, int n) {
        int nArgs = prepFuncCall(fi, node, a);
        fi.emitCall(node.getLine(), a, nArgs, n);
    }

    // return f(args)
    static void processTailCallExp(FuncInfo fi, FuncCallExp node, int a) {
        int nArgs = prepFuncCall(fi, node, a);
        fi.emitTailCall(node.getLine(), a, nArgs);
    }

    private static int prepFuncCall(FuncInfo fi, FuncCallExp node, int a) {
        List<Exp> args = node.getArgs();
        if (args == null) {
            args = Collections.emptyList();
        }
        int nArgs = args.size();
        boolean lastArgIsVarargOrFuncCall = false;

        processExp(fi, node.getPrefixExp(), a, 1);
        if (node.getNameExp() != null) {
            fi.allocReg();
            ArgAndKind argAndKindC = expToOpArg(fi, node.getNameExp(), ARG_RK);
            fi.emitSelf(node.getLine(), a, a, argAndKindC.arg);
            if (argAndKindC.kind == ARG_REG) {
                fi.freeRegs(1);
            }
        }
        for (int i = 0; i < args.size(); i++) {
            Exp arg = args.get(i);
            int tmp = fi.allocReg();
            if (i == nArgs-1 && ExpHelper.isVarargOrFuncCall(arg)) {
                lastArgIsVarargOrFuncCall = true;
                processExp(fi, arg, tmp, -1);
            } else {
                processExp(fi, arg, tmp, 1);
            }
        }
        fi.freeRegs(nArgs);

        if (node.getNameExp() != null) {
            fi.freeReg();
            nArgs++;
        }
        if (lastArgIsVarargOrFuncCall) {
            nArgs = -1;
        }

        return nArgs;
    }

    static ArgAndKind expToOpArg(FuncInfo fi, Exp node, int argKinds) {
        ArgAndKind ak = new ArgAndKind();

        if ((argKinds & ARG_CONST) > 0) {
            int idx = -1;
            if (node instanceof NilExp) {
                idx = fi.indexOfConstant(null);
            } else if (node instanceof FalseExp) {
                idx = fi.indexOfConstant(false);
            } else if (node instanceof TrueExp) {
                idx = fi.indexOfConstant(true);
            } else if (node instanceof IntegerExp) {
                idx = fi.indexOfConstant(((IntegerExp) node).getVal());
            } else if (node instanceof FloatExp) {
                idx = fi.indexOfConstant(((FloatExp) node).getVal());
            } else if (node instanceof StringExp) {
                idx = fi.indexOfConstant(((StringExp) node).getStr());
            }
            if (idx >= 0 && idx <= 0xFF) {
                ak.arg = 0x100 + idx;
                ak.kind = ARG_CONST;
                return ak;
            }
        }

        if (node instanceof NameExp) {
            if ((argKinds & ARG_REG) > 0) {
                int r = fi.slotOfLocVar(((NameExp) node).getName());
                if (r >= 0) {
                    ak.arg = r;
                    ak.kind = ARG_REG;
                    return ak;
                }
            }
            if ((argKinds & ARG_UPVAL) > 0) {
                int idx = fi.indexOfUpval(((NameExp) node).getName());
                if (idx >= 0) {
                    ak.arg = idx;
                    ak.kind = ARG_UPVAL;
                    return ak;
                }
            }
        }

        int a = fi.allocReg();
        processExp(fi, node, a, 1);
        ak.arg = a;
        ak.kind = ARG_REG;
        return ak;
    }

}
