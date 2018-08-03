package com.github.zxh0.luago.compiler.parser;

import com.github.zxh0.luago.compiler.ast.Block;
import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.exps.*;
import com.github.zxh0.luago.compiler.lexer.Lexer;
import com.github.zxh0.luago.compiler.lexer.Token;
import com.github.zxh0.luago.compiler.lexer.TokenKind;
import com.github.zxh0.luago.number.LuaNumber;

import java.util.ArrayList;
import java.util.List;

import static com.github.zxh0.luago.compiler.lexer.TokenKind.*;
import static com.github.zxh0.luago.compiler.parser.BlockParser.parseBlock;
import static com.github.zxh0.luago.compiler.parser.Optimizer.*;
import static com.github.zxh0.luago.compiler.parser.PrefixExpParser.parsePrefixExp;

class ExpParser {

    // explist ::= exp {‘,’ exp}
    static List<Exp> parseExpList(Lexer lexer) {
        List <Exp> exps = new ArrayList<>();
        exps.add(parseExp(lexer));
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            lexer.nextToken();
            exps.add(parseExp(lexer));
        }
        return exps;
    }

    /*
    exp ::=  nil | false | true | Numeral | LiteralString | ‘...’ | functiondef |
         prefixexp | tableconstructor | exp binop exp | unop exp
    */
    /*
    exp   ::= exp12
    exp12 ::= exp11 {or exp11}
    exp11 ::= exp10 {and exp10}
    exp10 ::= exp9 {(‘<’ | ‘>’ | ‘<=’ | ‘>=’ | ‘~=’ | ‘==’) exp9}
    exp9  ::= exp8 {‘|’ exp8}
    exp8  ::= exp7 {‘~’ exp7}
    exp7  ::= exp6 {‘&’ exp6}
    exp6  ::= exp5 {(‘<<’ | ‘>>’) exp5}
    exp5  ::= exp4 {‘..’ exp4}
    exp4  ::= exp3 {(‘+’ | ‘-’ | ‘*’ | ‘/’ | ‘//’ | ‘%’) exp3}
    exp2  ::= {(‘not’ | ‘#’ | ‘-’ | ‘~’)} exp1
    exp1  ::= exp0 {‘^’ exp2}
    exp0  ::= nil | false | true | Numeral | LiteralString
            | ‘...’ | functiondef | prefixexp | tableconstructor
    */
    static Exp parseExp(Lexer lexer) {
        return parseExp12(lexer);
    }


    // x or y
    private static Exp parseExp12(Lexer lexer) {
        Exp exp = parseExp11(lexer);
        while (lexer.LookAhead() == TOKEN_OP_OR) {
            Token op = lexer.nextToken();
            BinopExp lor = new BinopExp(op, exp, parseExp11(lexer));
            exp = optimizeLogicalOr(lor);
        }
        return exp;
    }

    // x and y
    private static Exp parseExp11(Lexer lexer) {
        Exp exp = parseExp10(lexer);
        while (lexer.LookAhead() == TOKEN_OP_AND) {
            Token op = lexer.nextToken();
            BinopExp land = new BinopExp(op, exp, parseExp10(lexer));
            exp = optimizeLogicalAnd(land);
        }
        return exp;
    }

    // compare
    private static Exp parseExp10(Lexer lexer) {
        Exp exp = parseExp9(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_LT:
                case TOKEN_OP_GT:
                case TOKEN_OP_NE:
                case TOKEN_OP_LE:
                case TOKEN_OP_GE:
                case TOKEN_OP_EQ:
                    Token op = lexer.nextToken();
                    exp = new BinopExp(op, exp, parseExp9(lexer));
                    break;
                default:
                    return exp;
            }
        }
    }

    // x | y
    private static Exp parseExp9(Lexer lexer) {
        Exp exp = parseExp8(lexer);
        while (lexer.LookAhead() == TOKEN_OP_BOR) {
            Token op = lexer.nextToken();
            BinopExp bor = new BinopExp(op, exp, parseExp8(lexer));
            exp = optimizeBitwiseBinaryOp(bor);
        }
        return exp;
    }

    // x ~ y
    private static Exp parseExp8(Lexer lexer) {
        Exp exp = parseExp7(lexer);
        while (lexer.LookAhead() == TOKEN_OP_WAVE) {
            Token op = lexer.nextToken();
            BinopExp bxor = new BinopExp(op, exp, parseExp7(lexer));
            exp = optimizeBitwiseBinaryOp(bxor);
        }
        return exp;
    }

    // x & y
    private static Exp parseExp7(Lexer lexer) {
        Exp exp = parseExp6(lexer);
        while (lexer.LookAhead() == TOKEN_OP_BAND) {
            Token op = lexer.nextToken();
            BinopExp band = new BinopExp(op, exp, parseExp6(lexer));
            exp = optimizeBitwiseBinaryOp(band);
        }
        return exp;
    }

    // shift
    private static Exp parseExp6(Lexer lexer) {
        Exp exp = parseExp5(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_SHL:
                case TOKEN_OP_SHR:
                    Token op = lexer.nextToken();
                    BinopExp shx = new BinopExp(op, exp, parseExp5(lexer));
                    exp = optimizeBitwiseBinaryOp(shx);
                    break;
                default:
                    return exp;
            }
        }
    }

    // a .. b
    private static Exp parseExp5(Lexer lexer) {
        Exp exp = parseExp4(lexer);
        if (lexer.LookAhead() != TOKEN_OP_CONCAT) {
            return exp;
        }

        List<Exp> exps = new ArrayList<>();
        exps.add(exp);
        int line = 0;
        while (lexer.LookAhead() == TOKEN_OP_CONCAT) {
            line = lexer.nextToken().getLine();
            exps.add(parseExp4(lexer));
        }
        return new ConcatExp(line, exps);
    }

    // x +/- y
    private static Exp parseExp4(Lexer lexer) {
        Exp exp = parseExp3(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_ADD:
                case TOKEN_OP_MINUS:
                    Token op = lexer.nextToken();
                    BinopExp arith = new BinopExp(op, exp, parseExp3(lexer));
                    exp = optimizeArithBinaryOp(arith);
                    break;
                default:
                    return exp;
            }
        }
    }

    // *, %, /, //
    private static Exp parseExp3(Lexer lexer) {
        Exp exp = parseExp2(lexer);
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_OP_MUL:
                case TOKEN_OP_MOD:
                case TOKEN_OP_DIV:
                case TOKEN_OP_IDIV:
                    Token op = lexer.nextToken();
                    BinopExp arith = new BinopExp(op, exp, parseExp2(lexer));
                    exp = optimizeArithBinaryOp(arith);
                    break;
                default:
                    return exp;
            }
        }
    }

    // unary
    private static Exp parseExp2(Lexer lexer) {
        switch (lexer.LookAhead()) {
            case TOKEN_OP_MINUS:
            case TOKEN_OP_WAVE:
            case TOKEN_OP_LEN:
            case TOKEN_OP_NOT:
                Token op = lexer.nextToken();
                UnopExp exp = new UnopExp(op, parseExp2(lexer));
            return optimizeUnaryOp(exp);
        }
        return parseExp1(lexer);
    }

    // x ^ y
    private static Exp parseExp1(Lexer lexer) { // pow is right associative
        Exp exp = parseExp0(lexer);
        if (lexer.LookAhead() == TOKEN_OP_POW) {
            Token op = lexer.nextToken();
            exp = new BinopExp(op, exp, parseExp2(lexer));
        }
        return optimizePow(exp);
    }

    private static Exp parseExp0(Lexer lexer) {
        switch (lexer.LookAhead()) {
            case TOKEN_VARARG: // ...
                return new VarargExp(lexer.nextToken().getLine());
            case TOKEN_KW_NIL: // nil
                return new NilExp(lexer.nextToken().getLine());
            case TOKEN_KW_TRUE: // true
                return new TrueExp(lexer.nextToken().getLine());
            case TOKEN_KW_FALSE: // false
                return new FalseExp(lexer.nextToken().getLine());
            case TOKEN_STRING: // LiteralString
                return new StringExp(lexer.nextToken());
            case TOKEN_NUMBER: // Numeral
                return parseNumberExp(lexer);
            case TOKEN_SEP_LCURLY: // tableconstructor
                return parseTableConstructorExp(lexer);
            case TOKEN_KW_FUNCTION: // functiondef
                lexer.nextToken();
                return parseFuncDefExp(lexer);
            default: // prefixexp
                return parsePrefixExp(lexer);
        }
    }

    private static Exp parseNumberExp(Lexer lexer) {
        Token token = lexer.nextToken();
        Long i = LuaNumber.parseInteger(token.getValue());
        if (i != null) {
            return new IntegerExp(token.getLine(), i);
        }
        Double f = LuaNumber.parseFloat(token.getValue());
        if (f != null) {
            return new FloatExp(token.getLine(), f);
        }
        throw new RuntimeException("not a number: " + token);
    }

    // functiondef ::= function funcbody
    // funcbody ::= ‘(’ [parlist] ‘)’ block end
    static FuncDefExp parseFuncDefExp(Lexer lexer) {
        int line = lexer.line();                    // function
        lexer.nextTokenOfKind(TOKEN_SEP_LPAREN);    // (
        List<String> parList = parseParList(lexer); // [parlist]
        lexer.nextTokenOfKind(TOKEN_SEP_RPAREN);    // )
        Block block = parseBlock(lexer);            // block
        lexer.nextTokenOfKind(TOKEN_KW_END);        // end
        int lastLine = lexer.line();

        FuncDefExp fdExp = new FuncDefExp();
        fdExp.setLine(line);
        fdExp.setLastLine(lastLine);
        fdExp.setIsVararg(parList.remove("..."));
        fdExp.setParList(parList);
        fdExp.setBlock(block);
        return fdExp;
    }

    // [parlist]
    // parlist ::= namelist [‘,’ ‘...’] | ‘...’
    private static List<String> parseParList(Lexer lexer) {
        List<String> names = new ArrayList<>();

        switch (lexer.LookAhead()) {
            case TOKEN_SEP_RPAREN:
                return names;
            case TOKEN_VARARG:
                lexer.nextToken();
                names.add("...");
                return names;
        }

        names.add(lexer.nextIdentifier().getValue());
        while (lexer.LookAhead() == TOKEN_SEP_COMMA) {
            lexer.nextToken();
            if (lexer.LookAhead() == TOKEN_IDENTIFIER) {
                names.add(lexer.nextIdentifier().getValue());
            } else {
                lexer.nextTokenOfKind(TOKEN_VARARG);
                names.add("...");
                break;
            }
        }

        return names;
    }

    // tableconstructor ::= ‘{’ [fieldlist] ‘}’
    static TableConstructorExp parseTableConstructorExp(Lexer lexer) {
        TableConstructorExp tcExp = new TableConstructorExp();
        tcExp.setLine(lexer.line());
        lexer.nextTokenOfKind(TOKEN_SEP_LCURLY); // {
        parseFieldList(lexer, tcExp);            // [fieldlist]
        lexer.nextTokenOfKind(TOKEN_SEP_RCURLY); // }
        tcExp.setLastLine(lexer.line());
        return tcExp;
    }

    // fieldlist ::= field {fieldsep field} [fieldsep]
    private static void parseFieldList(Lexer lexer, TableConstructorExp tcExp) {
        if (lexer.LookAhead() != TOKEN_SEP_RCURLY) {
            parseField(lexer, tcExp);

            while (isFieldSep(lexer.LookAhead())) {
                lexer.nextToken();
                if (lexer.LookAhead() != TOKEN_SEP_RCURLY) {
                    parseField(lexer, tcExp);
                } else {
                    break;
                }
            }
        }
    }

    // fieldsep ::= ‘,’ | ‘;’
    private static boolean isFieldSep(TokenKind kind) {
        return kind == TOKEN_SEP_COMMA || kind == TOKEN_SEP_SEMI;
    }

    // field ::= ‘[’ exp ‘]’ ‘=’ exp | Name ‘=’ exp | exp
    private static void parseField(Lexer lexer, TableConstructorExp tcExp) {
        if (lexer.LookAhead() == TOKEN_SEP_LBRACK) {
            lexer.nextToken();                       // [
            tcExp.addKey(parseExp(lexer));           // exp
            lexer.nextTokenOfKind(TOKEN_SEP_RBRACK); // ]
            lexer.nextTokenOfKind(TOKEN_OP_ASSIGN);  // =
            tcExp.addVal(parseExp(lexer));           // exp
            return;
        }

        Exp exp = parseExp(lexer);
        if (exp instanceof NameExp) {
            if (lexer.LookAhead() == TOKEN_OP_ASSIGN) {
                // Name ‘=’ exp => ‘[’ LiteralString ‘]’ = exp
                tcExp.addKey(new StringExp(exp.getLine(), ((NameExp) exp).getName()));
                lexer.nextToken();
                tcExp.addVal(parseExp(lexer));
                return;
            }
        }

        tcExp.addKey(null);
        tcExp.addVal(exp);
    }

}
