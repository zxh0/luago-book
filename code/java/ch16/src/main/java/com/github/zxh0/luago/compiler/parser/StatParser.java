package com.github.zxh0.luago.compiler.parser;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.Stat;
import com.github.zxh0.luago.compiler.ast.exps.*;
import com.github.zxh0.luago.compiler.ast.stats.*;
import com.github.zxh0.luago.compiler.lexer.Lexer;
import com.github.zxh0.luago.compiler.lexer.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.zxh0.luago.compiler.lexer.TokenKind.*;
import static com.github.zxh0.luago.compiler.parser.BlockParser.parseBlock;
import static com.github.zxh0.luago.compiler.parser.ExpParser.parseExp;
import static com.github.zxh0.luago.compiler.parser.ExpParser.parseExpList;
import static com.github.zxh0.luago.compiler.parser.ExpParser.parseFuncDefExp;
import static com.github.zxh0.luago.compiler.parser.PrefixExpParser.parsePrefixExp;

class StatParser {

    /*
    stat ::=  ‘;’
        | break
        | ‘::’ Name ‘::’
        | goto Name
        | do block end
        | while exp do block end
        | repeat block until exp
        | if exp then block {elseif exp then block} [else block] end
        | for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
        | for namelist in explist do block end
        | function funcname funcbody
        | local function Name funcbody
        | local namelist [‘=’ explist]
        | varlist ‘=’ explist
        | functioncall
    */
    static Stat parseStat(Lexer lexer) {
        switch (lexer.LookAhead()) {
            case TOKEN_SEP_SEMI:    return parseEmptyStat(lexer);
            case TOKEN_KW_BREAK:    return parseBreakStat(lexer);
            case TOKEN_SEP_LABEL:   return parseLabelStat(lexer);
            case TOKEN_KW_GOTO:     return parseGotoStat(lexer);
            case TOKEN_KW_DO:       return parseDoStat(lexer);
            case TOKEN_KW_WHILE:    return parseWhileStat(lexer);
            case TOKEN_KW_REPEAT:   return parseRepeatStat(lexer);
            case TOKEN_KW_IF:       return parseIfStat(lexer);
            case TOKEN_KW_FOR:      return parseForStat(lexer);
            case TOKEN_KW_FUNCTION: return parseFuncDefStat(lexer);
            case TOKEN_KW_LOCAL:    return parseLocalAssignOrFuncDefStat(lexer);
            default:                return parseAssignOrFuncCallStat(lexer);
        }
    }

    // ;
    private static EmptyStat parseEmptyStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_SEP_SEMI);
        return EmptyStat.INSTANCE;
    }

    // break
    private static BreakStat parseBreakStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_BREAK);
        return new BreakStat(lexer.line());
    }

    // ‘::’ Name ‘::’
    private static LabelStat parseLabelStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_SEP_LABEL);          // ::
        String name = lexer.nextIdentifier().getValue(); // name
        lexer.nextTokenOfKind(TOKEN_SEP_LABEL);          // ::
        return new LabelStat(name);
    }

    // goto Name
    private static GotoStat parseGotoStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_GOTO);            // goto
        String name = lexer.nextIdentifier().getValue(); // name
        return new GotoStat(name);
    }

    // do block end
    private static DoStat parseDoStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_DO);  // do
        Block block = parseBlock(lexer);     // block
        lexer.nextTokenOfKind(TOKEN_KW_END); // end
        return new DoStat(block);
    }

    // while exp do block end
    private static WhileStat parseWhileStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_WHILE); // while
        Exp exp = parseExp(lexer);             // exp
        lexer.nextTokenOfKind(TOKEN_KW_DO);    // do
        Block block = parseBlock(lexer);       // block
        lexer.nextTokenOfKind(TOKEN_KW_END);   // end
        return new WhileStat(exp, block);
    }

    // repeat block until exp
    private static RepeatStat parseRepeatStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_REPEAT); // repeat
        Block block = parseBlock(lexer);        // block
        lexer.nextTokenOfKind(TOKEN_KW_UNTIL);  // until
        Exp exp = parseExp(lexer);              // exp
        return new RepeatStat(block, exp);
    }

    // if exp then block {elseif exp then block} [else block] end
    private static IfStat parseIfStat(Lexer lexer) {
        List<Exp> exps = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();

        lexer.nextTokenOfKind(TOKEN_KW_IF);       // if
        exps.add(parseExp(lexer));                // exp
        lexer.nextTokenOfKind(TOKEN_KW_THEN);     // then
        blocks.add(parseBlock(lexer));            // block

        while (lexer.LookAhead() == TOKEN_KW_ELSEIF) {
            lexer.nextToken();                    // elseif
            exps.add(parseExp(lexer));            // exp
            lexer.nextTokenOfKind(TOKEN_KW_THEN); // then
            blocks.add(parseBlock(lexer));        // block
        }

        // else block => elseif true then block
        if (lexer.LookAhead() == TOKEN_KW_ELSE) {
            lexer.nextToken();                    // else
            exps.add(new TrueExp(lexer.line()));  //
            blocks.add(parseBlock(lexer));        // block
        }

        lexer.nextTokenOfKind(TOKEN_KW_END);      // end
        return new IfStat(exps, blocks);
    }

    // for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
    // for namelist in explist do block end
    private static Stat parseForStat(Lexer lexer) {
        int lineOfFor = lexer.nextTokenOfKind(TOKEN_KW_FOR).getLine();
        String name = lexer.nextIdentifier().getValue();
        if (lexer.LookAhead() == TOKEN_OP_ASSIGN) {
            return finishForNumStat(lexer, name, lineOfFor);
        } else {
            return finishForInStat(lexer, name);
        }
    }

    // for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
    private static ForNumStat finishForNumStat(Lexer lexer, String name, int lineOfFor) {
        ForNumStat stat = new ForNumStat();
        stat.setLineOfFor(lineOfFor);           // for
        stat.setVarName(name);                  // name

        lexer.nextTokenOfKind(TOKEN_OP_ASSIGN); // =
        stat.setInitExp(parseExp(lexer));       // exp
        lexer.nextTokenOfKind(TOKEN_SEP_COMMA); // ,
        stat.setLimitExp(parseExp(lexer));      // exp

        if (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            lexer.nextToken();                  // ,
            stat.setStepExp(parseExp(lexer));   // exp
        } else {
            stat.setStepExp(new IntegerExp(lexer.line(), 1));
        }

        lexer.nextTokenOfKind(TOKEN_KW_DO);     // do
        stat.setLineOfDo(lexer.line());         //
        stat.setBlock(parseBlock(lexer));       // block
        lexer.nextTokenOfKind(TOKEN_KW_END);    // end

        return stat;
    }

    // for namelist in explist do block end
    // namelist ::= Name {‘,’ Name}
    // explist ::= exp {‘,’ exp}
    private static ForInStat finishForInStat(Lexer lexer, String name0) {
        ForInStat stat = new ForInStat();
                                                        // for
        stat.setNameList(finishNameList(lexer, name0)); // namelist
        lexer.nextTokenOfKind(TOKEN_KW_IN);             // in
        stat.setExpList(parseExpList(lexer));           // explist
        lexer.nextTokenOfKind(TOKEN_KW_DO);             // do
        stat.setLineOfDo(lexer.line());                 //
        stat.setBlock(parseBlock(lexer));               // block
        lexer.nextTokenOfKind(TOKEN_KW_END);            // end

        return stat;
    }

    // namelist ::= Name {‘,’ Name}
    private static List<String> finishNameList(Lexer lexer, String name0) {
        List<String> names = new ArrayList<>();
        names.add(name0);
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            lexer.nextToken();                            // ,
            names.add(lexer.nextIdentifier().getValue()); // Name
        }
        return names;
    }

    // local function Name funcbody
    // local namelist [‘=’ explist]
    private static Stat parseLocalAssignOrFuncDefStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_LOCAL);
        if (lexer.LookAhead() == TOKEN_KW_FUNCTION) {
            return finishLocalFuncDefStat(lexer);
        } else {
            return finishLocalVarDeclStat(lexer);
        }
    }

    /*
    http://www.lua.org/manual/5.3/manual.html#3.4.11

    function f() end          =>  f = function() end
    function t.a.b.c.f() end  =>  t.a.b.c.f = function() end
    function t.a.b.c:f() end  =>  t.a.b.c.f = function(self) end
    local function f() end    =>  local f; f = function() end

    The statement `local function f () body end`
    translates to `local f; f = function () body end`
    not to `local f = function () body end`
    (This only makes a difference when the body of the function
     contains references to f.)
    */
    // local function Name funcbody
    private static LocalFuncDefStat finishLocalFuncDefStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_FUNCTION);        // local function
        String name = lexer.nextIdentifier().getValue(); // name
        FuncDefExp fdExp = parseFuncDefExp(lexer);       // funcbody
        return new LocalFuncDefStat(name, fdExp);
    }

    // local namelist [‘=’ explist]
    private static LocalVarDeclStat finishLocalVarDeclStat(Lexer lexer) {
        String name0 = lexer.nextIdentifier().getValue();     // local Name
        List<String> nameList = finishNameList(lexer, name0); // { , Name }
        List<Exp> expList = null;
        if (lexer.LookAhead() == TOKEN_OP_ASSIGN) {
            lexer.nextToken();                                // ==
            expList = parseExpList(lexer);                    // explist
        }
        int lastLine = lexer.line();
        return new LocalVarDeclStat(lastLine, nameList, expList);
    }

    // varlist ‘=’ explist
    // functioncall
    private static Stat parseAssignOrFuncCallStat(Lexer lexer) {
        Exp prefixExp = parsePrefixExp(lexer);
        if (prefixExp instanceof FuncCallExp) {
            return new FuncCallStat((FuncCallExp) prefixExp);
        } else {
            return parseAssignStat(lexer, prefixExp);
        }
    }

    // varlist ‘=’ explist |
    private static AssignStat parseAssignStat(Lexer lexer, Exp var0) {
        List<Exp> varList = finishVarList(lexer, var0); // varlist
        lexer.nextTokenOfKind(TOKEN_OP_ASSIGN);         // =
        List<Exp> expList = parseExpList(lexer);        // explist
        int lastLine = lexer.line();
        return new AssignStat(lastLine, varList, expList);
    }

    // varlist ::= var {‘,’ var}
    private static List<Exp> finishVarList(Lexer lexer, Exp var0) {
        List<Exp> vars = new ArrayList<>();
        vars.add(checkVar(lexer, var0));               // var
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) { // {
            lexer.nextToken();                         // ,
            Exp exp = parsePrefixExp(lexer);           // var
            vars.add(checkVar(lexer, exp));            //
        }                                              // }
        return vars;
    }

    // var ::=  Name | prefixexp ‘[’ exp ‘]’ | prefixexp ‘.’ Name
    private static Exp checkVar(Lexer lexer, Exp exp) {
        if (exp instanceof NameExp || exp instanceof TableAccessExp) {
            return exp;
        }
        lexer.nextTokenOfKind(null); // trigger error
        throw new RuntimeException("unreachable!");
    }

    // function funcname funcbody
    // funcname ::= Name {‘.’ Name} [‘:’ Name]
    // funcbody ::= ‘(’ [parlist] ‘)’ block end
    // parlist ::= namelist [‘,’ ‘...’] | ‘...’
    // namelist ::= Name {‘,’ Name}
    private static AssignStat parseFuncDefStat(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_KW_FUNCTION);     // function
        Map<Exp, Boolean> map = parseFuncName(lexer); // funcname
        Exp fnExp = map.keySet().iterator().next();
        boolean hasColon = map.values().iterator().next();
        FuncDefExp fdExp = parseFuncDefExp(lexer);    // funcbody
        if (hasColon) { // insert self
            if (fdExp.getParList() == null) {
                fdExp.setParList(new ArrayList<>());
            }
            fdExp.getParList().add(0, "self");
        }

        return new AssignStat(fdExp.getLastLine(),
                Collections.singletonList(fnExp),
                Collections.singletonList(fdExp));
    }

    // funcname ::= Name {‘.’ Name} [‘:’ Name]
    private static Map<Exp, Boolean> parseFuncName(Lexer lexer) {
        Token id = lexer.nextIdentifier();
        Exp exp = new NameExp(id.getLine(), id.getValue());
        boolean hasColon = false;

        while (lexer.LookAhead() == TOKEN_SEP_DOT) {
            lexer.nextToken();
            id = lexer.nextIdentifier();
            Exp idx = new StringExp(id);
            exp = new TableAccessExp(id.getLine(), exp, idx);
        }
        if (lexer.LookAhead() == TOKEN_SEP_COLON) {
            lexer.nextToken();
            id = lexer.nextIdentifier();
            Exp idx = new StringExp(id);
            exp = new TableAccessExp(id.getLine(), exp, idx);
            hasColon = true;
        }

        // workaround: return multiple values
        return Collections.singletonMap(exp, hasColon);
    }

}
