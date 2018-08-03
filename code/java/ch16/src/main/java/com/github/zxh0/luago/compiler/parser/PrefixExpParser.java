package com.github.zxh0.luago.compiler.parser;

import com.github.zxh0.luago.compiler.ast.Exp;
import com.github.zxh0.luago.compiler.ast.exps.*;
import com.github.zxh0.luago.compiler.lexer.Lexer;
import com.github.zxh0.luago.compiler.lexer.Token;

import java.util.Collections;
import java.util.List;

import static com.github.zxh0.luago.compiler.lexer.TokenKind.*;
import static com.github.zxh0.luago.compiler.parser.ExpParser.parseExp;
import static com.github.zxh0.luago.compiler.parser.ExpParser.parseExpList;
import static com.github.zxh0.luago.compiler.parser.ExpParser.parseTableConstructorExp;

class PrefixExpParser {

    /*
    prefixexp ::= Name
        | ‘(’ exp ‘)’
        | prefixexp ‘[’ exp ‘]’
        | prefixexp ‘.’ Name
        | prefixexp [‘:’ Name] args
    */
    static Exp parsePrefixExp(Lexer lexer) {
        Exp exp;
        if (lexer.LookAhead() == TOKEN_IDENTIFIER) {
            Token id = lexer.nextIdentifier(); // Name
            exp = new NameExp(id.getLine(), id.getValue());
        } else { // ‘(’ exp ‘)’
            exp = parseParensExp(lexer);
        }
        return finishPrefixExp(lexer, exp);
    }

    private static Exp parseParensExp(Lexer lexer) {
        lexer.nextTokenOfKind(TOKEN_SEP_LPAREN); // (
        Exp exp = parseExp(lexer);               // exp
        lexer.nextTokenOfKind(TOKEN_SEP_RPAREN); // )

        if (exp instanceof VarargExp
                || exp instanceof FuncCallExp
                || exp instanceof NameExp
                || exp instanceof TableAccessExp) {
            return new ParensExp(exp);
        }

        // no need to keep parens
        return exp;
    }

    private static Exp finishPrefixExp(Lexer lexer, Exp exp) {
        while (true) {
            switch (lexer.LookAhead()) {
                case TOKEN_SEP_LBRACK: { // prefixexp ‘[’ exp ‘]’
                    lexer.nextToken();                       // ‘[’
                    Exp keyExp = parseExp(lexer);            // exp
                    lexer.nextTokenOfKind(TOKEN_SEP_RBRACK); // ‘]’
                    exp = new TableAccessExp(lexer.line(), exp, keyExp);
                    break;
                }
                case TOKEN_SEP_DOT: { // prefixexp ‘.’ Name
                    lexer.nextToken();                   // ‘.’
                    Token name = lexer.nextIdentifier(); // Name
                    Exp keyExp = new StringExp(name);
                    exp = new TableAccessExp(name.getLine(), exp, keyExp);
                    break;
                }
                case TOKEN_SEP_COLON: // prefixexp ‘:’ Name args
                case TOKEN_SEP_LPAREN:
                case TOKEN_SEP_LCURLY:
                case TOKEN_STRING: // prefixexp args
                    exp = finishFuncCallExp(lexer, exp);
                    break;
                default:
                    return exp;
            }
        }
    }

    // functioncall ::=  prefixexp args | prefixexp ‘:’ Name args
    private static FuncCallExp finishFuncCallExp(Lexer lexer, Exp prefixExp) {
        FuncCallExp fcExp = new FuncCallExp();
        fcExp.setPrefixExp(prefixExp);
        fcExp.setNameExp(parseNameExp(lexer));
        fcExp.setLine(lexer.line()); // todo
        fcExp.setArgs(parseArgs(lexer));
        fcExp.setLastLine(lexer.line());
        return fcExp;
    }

    private static StringExp parseNameExp(Lexer lexer) {
        if (lexer.LookAhead() == TOKEN_SEP_COLON) {
            lexer.nextToken();
            Token name = lexer.nextIdentifier();
            return new StringExp(name);
        }
        return null;
    }

    // args ::=  ‘(’ [explist] ‘)’ | tableconstructor | LiteralString
    private static List<Exp> parseArgs(Lexer lexer) {
        switch (lexer.LookAhead()) {
            case TOKEN_SEP_LPAREN: // ‘(’ [explist] ‘)’
                lexer.nextToken(); // TOKEN_SEP_LPAREN
                List<Exp> args = null;
                if (lexer.LookAhead() != TOKEN_SEP_RPAREN) {
                    args = parseExpList(lexer);
                }
                lexer.nextTokenOfKind(TOKEN_SEP_RPAREN);
                return args;
            case TOKEN_SEP_LCURLY: // ‘{’ [fieldlist] ‘}’
                return Collections.singletonList(parseTableConstructorExp(lexer));
            default: // LiteralString
                Token str = lexer.nextTokenOfKind(TOKEN_STRING);
                return Collections.singletonList(new StringExp(str));
        }
    }

}
