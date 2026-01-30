package com.github.tilcob.game.yarn.expression;

import com.github.tilcob.game.flow.CommandCall;

import java.util.ArrayList;
import java.util.List;

public class ExpressionLexer {
    private final String expression;
    private final int len;
    private final CommandCall.SourcePos source;
    private int i = 0;
    private List<ExpressionToken> tokens;

    public ExpressionLexer(String expression, CommandCall.SourcePos source) {
        this.expression = expression == null ? "" : expression;
        this.len = this.expression.length();
        this.source = source == null ? CommandCall.SourcePos.unknown() : source;
    }

    public List<ExpressionToken> lex() {
        tokens = new ArrayList<>();

        while (!isAtEnd()) {
            int before = i;

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
                case '+' -> addToken(ExpressionTokenType.ADD, "+", start);
                case '-' -> {
                    if (peekDigit()) {
                        addToken(readNumber(start));
                    } else {
                        addToken(ExpressionTokenType.SUBTRACT, "-", start);
                    }
                }
                case '*' -> addToken(ExpressionTokenType.MULTIPLY, "*", start);
                case '/' -> addToken(ExpressionTokenType.DIVIDE, "/", start);

                default -> {
                    if (isDigit(c)) {
                        addToken(readNumber(start));
                    } else if (isIdentStart(c)) {
                        addToken(readIdent(start));
                    } else {
                        throw error(start, "Unexpected character: '" + c + "'");
                    }
                }
            }

            if (before == i) {
                throw new IllegalStateException("Lexer made no progress at index " + i +
                    " near '" + (isAtEnd() ? "<eof>" : peek()) + "' in: " + expression);
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
        if (!isAtEnd() && peek() == '.' && (i + 1 < len) && isDigit(expression.charAt(i + 1))) {
            do advance();
            while (!isAtEnd() && isDigit(peek()));
        }
        String text = expression.substring(startPos, i);
        return new ExpressionToken(ExpressionTokenType.NUMBER, text, startPos);
    }

    private ExpressionToken readIdent(int startPos) {
        while (!isAtEnd() && isIdentPart(peek())) advance();
        String text = expression.substring(startPos, i);

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

    private char advance() { return expression.charAt(i++); }

    private char peek() { return expression.charAt(i); }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (expression.charAt(i) != expected) return false;
        i++;
        return true;
    }

    private boolean peekDigit() {
        if (isAtEnd()) return false;
        return isDigit(peek());
    }

    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }

    private boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private boolean isIdentPart(char c) {
        return isIdentStart(c) || isDigit(c);
    }

    private YarnExpressionException error(int pos, String msg) {
        return new YarnExpressionException(source, expression, pos, "Lexer error: " + msg);
    }
}
