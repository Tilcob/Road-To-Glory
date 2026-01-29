package com.github.tilcob.game.yarn.expression;

import java.util.ArrayList;
import java.util.List;

public class ExpressionLexer {
    private final String src;
    private final int len;
    private int i = 0;
    private List<ExpressionToken> tokens;

    public ExpressionLexer(String src) {
        this.src = src == null ? "" : src;
        this.len = this.src.length();
    }

    public List<ExpressionToken> lex() {
        tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            int start = i;
            char c = advance();

            switch (c) {
                case '(' -> addToken(ExpressionTokenType.LEFT_PARENTHESIS, "(", start);
                case ')' -> addToken(ExpressionTokenType.RIGHT_PARENTHESIS, ")", start);
                case ',' -> addToken(ExpressionTokenType.COMMA, ",", start);
                case '!' -> {
                    if (match('=')) addToken(ExpressionTokenType.NOT_EQUAL, "!=", start);
                    else addToken(ExpressionTokenType.NOT, "!", start);
                }
                case '=' -> {
                    if (match('=')) addToken(ExpressionTokenType.EQUAL, "==", start);
                    else throw error(start, "Unexpected '=' (did you mean '==')?");
                }
                case '>' -> {
                    if (match('=')) addToken(ExpressionTokenType.GREATER_OR_EQUAL, ">=", start);
                    else addToken(ExpressionTokenType.GREATER, ">", start);
                }
                case '<' -> {
                    if (match('=')) addToken(ExpressionTokenType.LESS_OR_EQUAL, "<=", start);
                    else addToken(ExpressionTokenType.LESS, "<", start);
                }
                case '&' -> {
                    if (match('&')) addToken(ExpressionTokenType.AND, "&&", start);
                    else throw error(start, "Unexpected '&' (did you mean '&&')?");
                }
                case '|' -> {
                    if (match('|')) addToken(ExpressionTokenType.OR, "||", start);
                    else throw error(start, "Unexpected '|' (did you mean '||')?");
                }
                case '"' -> addToken(readString(start));

                default -> {
                    if (isDigit(c) || (c == '-' && peekDigit())) {
                        addToken(readNumber(start));
                    } else if (isIdentStart(c)) {
                        addToken(readIdent(start));
                    } else {
                        throw error(start, "Unexpected character: '" + c + "'");
                    }
                }
            }
        }
        addToken(ExpressionTokenType.EOF, "", len);
        return tokens;
    }

    private ExpressionToken readString(int startPos) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd()) {
            char c = advance();
            if (c == '"') {
                return new ExpressionToken(ExpressionTokenType.STRING, sb.toString(), startPos);
            }
            if (c == '\\' && !isAtEnd()) {
                char n = advance();
                switch (n) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    default -> sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        throw error(startPos, "Unterminated string literal");
    }

    private ExpressionToken readNumber(int startPos) {
        while (!isAtEnd() && isDigit(peek())) advance();
        if (!isAtEnd() && peek() == '.' && (i + 1 < len) && isDigit(src.charAt(i + 1))) {
            do advance();
            while (!isAtEnd() && isDigit(peek()));
        }
        String text = src.substring(startPos, i);
        return new ExpressionToken(ExpressionTokenType.NUMBER, text, startPos);
    }

    private ExpressionToken readIdent(int startPos) {
        while (!isAtEnd() && isIdentPart(peek())) advance();
        String text = src.substring(startPos, i);

        return switch (text) {
            case "true" -> new ExpressionToken(ExpressionTokenType.TRUE, text, startPos);
            case "false" -> new ExpressionToken(ExpressionTokenType.FALSE, text, startPos);
            default -> new ExpressionToken(ExpressionTokenType.IDENT, text, startPos);
        };
    }

    private void addToken(ExpressionTokenType type, String text, int pos) {
        tokens.add(new ExpressionToken(type, text, pos));
    }

    private void addToken(ExpressionToken token) {
        tokens.add(token);
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') i++;
            else break;
        }
    }

    private boolean isAtEnd() { return i >= len; }

    private char advance() { return src.charAt(i++); }

    private char peek() { return src.charAt(i); }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (src.charAt(i) != expected) return false;
        i++;
        return true;
    }

    private boolean peekDigit() {
        if (isAtEnd()) return false;
        return isDigit(peek());
    }

    private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }

    private static boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private static boolean isIdentPart(char c) {
        return isIdentStart(c) || isDigit(c);
    }

    private static IllegalArgumentException error(int pos, String msg) {
        return new IllegalArgumentException("ExprLexer@" + pos + ": " + msg);
    }
}
