package com.github.zxh0.luago.compiler.lexer;

import java.util.regex.Pattern;

class Escaper {

    private static final Pattern reDecEscapeSeq = Pattern.compile("^\\\\[0-9]{1,3}");
    private static final Pattern reHexEscapeSeq = Pattern.compile("^\\\\x[0-9a-fA-F]{2}");
    private static final Pattern reUnicodeEscapeSeq = Pattern.compile("^\\\\u\\{[0-9a-fA-F]+}");

    private CharSeq rawStr;
    private Lexer lexer;
    private StringBuilder buf = new StringBuilder();

    Escaper(String rawStr, Lexer lexer) {
        this.rawStr = new CharSeq(rawStr);
        this.lexer = lexer;
    }

    String escape() {
        while (rawStr.length() > 0) {
            if (rawStr.charAt(0) != '\\') {
                buf.append(rawStr.nextChar());
                continue;
            }

            if (rawStr.length() == 1) {
                return lexer.error("unfinished string");
            }

            switch (rawStr.charAt(1)) {
                case 'a':  buf.append((char) 0x07); rawStr.next(2); continue; // Bell
                case 'v':  buf.append((char) 0x0B); rawStr.next(2); continue; // Vertical tab
                case 'b':  buf.append('\b'); rawStr.next(2); continue;
                case 'f':  buf.append('\f'); rawStr.next(2); continue;
                case 'n':  buf.append('\n'); rawStr.next(2); continue;
                case 'r':  buf.append('\r'); rawStr.next(2); continue;
                case 't':  buf.append('\t'); rawStr.next(2); continue;
                case '"':  buf.append('"');  rawStr.next(2); continue;
                case '\'': buf.append('\''); rawStr.next(2); continue;
                case '\\': buf.append('\\'); rawStr.next(2); continue;
                case '\n': buf.append('\n'); rawStr.next(2); continue;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': escapeDecSeq();     continue; // \ddd
                case 'x': escapeHexSeq();     continue; // \xXX
                case 'u': escapeUnicodeSeq(); continue; // \ u{XXX}
                case 'z':
                    rawStr.next(2);
                    skipWhitespaces();
                    continue;
            }
            reportInvalidEscapeSeq();
        }

        return buf.toString();
    }

    private void reportInvalidEscapeSeq() {
        lexer.error("invalid escape sequence near '\\%c'", rawStr.charAt(1));
    }

    // \ddd
    private void escapeDecSeq() {
        String seq = rawStr.find(reDecEscapeSeq);
        if (seq == null) {
            reportInvalidEscapeSeq();
        }

        try {
            int d = Integer.parseInt(seq.substring(1));
            if (d <= 0xFF) {
                buf.append((char) d);
                rawStr.next(seq.length());
                return;
            }
        } catch (NumberFormatException ignored) {}

        lexer.error("decimal escape too large near '%s'", seq);
    }

    // \xXX
    private void escapeHexSeq() {
        String seq = rawStr.find(reHexEscapeSeq);
        if (seq == null) {
            reportInvalidEscapeSeq();
        }

        int d = Integer.parseInt(seq.substring(2), 16);
        buf.append((char) d);
        rawStr.next(seq.length());
    }

    // \ u{XXX}
    private void escapeUnicodeSeq() {
        String seq = rawStr.find(reUnicodeEscapeSeq);
        if (seq == null) {
            reportInvalidEscapeSeq();
        }

        try {
            int d = Integer.parseInt(seq.substring(3, seq.length() - 1), 16);
            if (d <= 0x10FFFF) {
                buf.appendCodePoint(d);
                rawStr.next(seq.length());
                return;
            }
        } catch (NumberFormatException ignored) {}

        lexer.error("UTF-8 value too large near '%s'", seq);
    }

    private void skipWhitespaces() {
        while (rawStr.length() > 0
                && CharUtil.isWhiteSpace(rawStr.charAt(0))) {
            rawStr.next(1);
        }
    }

}
