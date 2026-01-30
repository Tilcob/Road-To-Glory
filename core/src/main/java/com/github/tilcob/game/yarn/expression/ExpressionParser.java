package com.github.tilcob.game.yarn.expression;

import com.github.tilcob.game.flow.CommandCall;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    private final List<ExpressionToken> tokens;
    private int index = 0;
    private final CommandCall.SourcePos sourcePos;
    private final String expression;

    public ExpressionParser(List<ExpressionToken> tokens, String expression, CommandCall.SourcePos sourcePos) {
        this.tokens = tokens;
        this.expression = expression == null ? "" : expression;
        this.sourcePos = sourcePos == null ? CommandCall.SourcePos.unknown() : sourcePos;
    }

    public Node parse() {
        Node node = parseOr();
        consume(ExpressionTokenType.EOF, "Expected end of expression");
        return node;
    }

    private Node parseOr() {
        Node left = parseAnd();
        while (match(ExpressionTokenType.OR)) {
            ExpressionToken op = previous();
            Node right = parseAnd();
            left = new Node.Binary(left, op.type(), right);
        }
        return left;
    }

    private Node parseAnd() {
        Node left = parseEquality();
        while (match(ExpressionTokenType.AND)) {
            ExpressionToken op = previous();
            Node right = parseEquality();
            left = new Node.Binary(left, op.type(), right);
        }
        return left;
    }

    private Node parseEquality() {
        Node left = parseComparison();
        while (match(ExpressionTokenType.EQUAL, ExpressionTokenType.NOT_EQUAL)) {
            ExpressionToken op = previous();
            Node right = parseComparison();
            left = new Node.Binary(left, op.type(), right);
        }
        return left;
    }

    private Node parseComparison() {
        Node left = parseTerm();
        while (match(ExpressionTokenType.LESS, ExpressionTokenType.LESS_OR_EQUAL,
            ExpressionTokenType.GREATER, ExpressionTokenType.GREATER_OR_EQUAL)) {
            ExpressionToken operator = previous();
            Node right = parseTerm();
            left = new Node.Binary(left, operator.type(), right);
        }
        return left;
    }

    private Node parseUnary() {
        if (match(ExpressionTokenType.NOT, ExpressionTokenType.SUBTRACT)) {
            ExpressionToken operator = previous();
            Node right = parseUnary();
            return new Node.Unary(operator.type(), right);
        }
        return parsePrimary();
    }

    private Node parsePrimary() {
        if (match(ExpressionTokenType.TRUE)) return new Node.Literal(true);
        if (match(ExpressionTokenType.FALSE)) return new Node.Literal(false);

        if (match(ExpressionTokenType.NUMBER)) {
            String raw = previous().value();
            if (raw.contains(".")) return new Node.Literal(Double.parseDouble(raw));
            return new Node.Literal(Double.parseDouble(raw)); // keep numeric comparisons uniform
        }

        if (match(ExpressionTokenType.STRING)) {
            return new Node.Literal(previous().value());
        }

        if (match(ExpressionTokenType.LEFT_PARENTHESIS)) {
            Node inner = parseOr();
            consume(ExpressionTokenType.RIGHT_PARENTHESIS, "Expected ')'");
            return inner;
        }

        if (match(ExpressionTokenType.IDENT)) {
            String name = previous().value();

            if (match(ExpressionTokenType.LEFT_PARENTHESIS)) {
                List<Node> args = new ArrayList<>();
                if (!check(ExpressionTokenType.RIGHT_PARENTHESIS)) {
                    do {
                        args.add(parseOr());
                    } while (match(ExpressionTokenType.COMMA));
                }
                consume(ExpressionTokenType.RIGHT_PARENTHESIS, "Expected ')'");
                return new Node.Call(name, args, true);
            }

            if (looksLikeArgStart(peek())) {
                List<Node> args = new ArrayList<>();
                while (looksLikeArgStart(peek())) {
                    args.add(parseArgAtom());
                }
                return new Node.Call(name, args, false);
            }

            return new Node.Var(name);
        }

        throw error(peek(), "Expected expression");
    }

    private Node parseArgAtom() {
        if (match(ExpressionTokenType.TRUE)) return new Node.Literal(true);
        if (match(ExpressionTokenType.FALSE)) return new Node.Literal(false);
        if (match(ExpressionTokenType.NUMBER)) return new Node.Literal(Double.parseDouble(previous().value()));
        if (match(ExpressionTokenType.STRING)) return new Node.Literal(previous().value());
        if (match(ExpressionTokenType.LEFT_PARENTHESIS)) {
            Node inner = parseOr();
            consume(ExpressionTokenType.RIGHT_PARENTHESIS, "Expected ')'");
            return inner;
        }
        if (match(ExpressionTokenType.IDENT)) return new Node.Var(previous().value());
        throw error(peek(), "Expected argument");
    }

    private Node parseTerm() {
        Node left = parseFactor();
        while (match(ExpressionTokenType.ADD, ExpressionTokenType.SUBTRACT)) {
            ExpressionToken op = previous();
            Node right = parseFactor();
            left = new Node.Binary(left, op.type(), right);
        }
        return left;
    }

    private Node parseFactor() {
        Node left = parseUnary();
        while (match(ExpressionTokenType.MULTIPLY, ExpressionTokenType.DIVIDE)) {
            ExpressionToken op = previous();
            Node right = parseUnary();
            left = new Node.Binary(left, op.type(), right);
        }
        return left;
    }

    private static boolean looksLikeArgStart(ExpressionToken tok) {
        return tok.type() == ExpressionTokenType.STRING
            || tok.type() == ExpressionTokenType.NUMBER
            || tok.type() == ExpressionTokenType.TRUE
            || tok.type() == ExpressionTokenType.FALSE
            || tok.type() == ExpressionTokenType.IDENT
            || tok.type() == ExpressionTokenType.LEFT_PARENTHESIS;
    }

    private boolean match(ExpressionTokenType... types) {
        for (ExpressionTokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private void consume(ExpressionTokenType type, String msg) {
        if (check(type)) {
            advance();
            return;
        }
        throw error(peek(), msg);
    }

    private boolean check(ExpressionTokenType type) {
        return peek().type() == type;
    }

    private void advance() {
        if (!isAtEnd()) index++;
    }

    private boolean isAtEnd() {
        return peek().type() == ExpressionTokenType.EOF;
    }

    private ExpressionToken peek() {
        return tokens.get(index);
    }

    private ExpressionToken previous() {
        return tokens.get(index - 1);
    }

    private YarnExpressionException error(ExpressionToken token, String msg) {
        return new YarnExpressionException(sourcePos, expression, token.pos(),
            "Parser error: " + msg + " (got " + token.type() + ")");
    }

    public sealed interface Node permits Node.Literal, Node.Var, Node.Call, Node.Unary, Node.Binary {
        record Literal(Object value) implements Node {}
        record Var(String name) implements Node {}
        record Call(String name, List<Node> args, boolean parenStyle) implements Node {}
        record Unary(ExpressionTokenType type, Node right) implements Node {}
        record Binary(Node left, ExpressionTokenType type, Node right) implements Node {}
    }
}
