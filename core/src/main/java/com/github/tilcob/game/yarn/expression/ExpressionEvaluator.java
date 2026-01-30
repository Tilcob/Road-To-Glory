package com.github.tilcob.game.yarn.expression;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.CommandCall;
import com.github.tilcob.game.flow.FlowContext;
import com.github.tilcob.game.flow.FunctionCall;
import com.github.tilcob.game.flow.FunctionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExpressionEvaluator {
    private final FunctionRegistry functionRegistry;
    private final VarResolver vars;

    public ExpressionEvaluator(FunctionRegistry functionRegistry, VarResolver vars) {
        this.functionRegistry = functionRegistry;
        this.vars = vars;
    }

    public boolean evalBool(Entity player, String expr, CommandCall.SourcePos sourcePos) {
        Object evaluationResult = eval(player, expr, sourcePos);
        return truthy(evaluationResult);
    }

    public Object eval(Entity player, String expr, CommandCall.SourcePos sourcePos) {
        try {
            List<ExpressionToken> tokens = new ExpressionLexer(expr, sourcePos).lex();
            ExpressionParser.Node parsed = new ExpressionParser(tokens, expr, sourcePos).parse();
            return evalNode(player, parsed, sourcePos);
        } catch (YarnExpressionException e) {
            throw e;
        } catch (Exception e) {
            throw new YarnExpressionException(
                sourcePos == null ? CommandCall.SourcePos.unknown() : sourcePos,
                expr,
                0,
                "Evaluation error: " + e.getMessage()
            );
        }
    }

    private Object evalNode(Entity player, ExpressionParser.Node node, CommandCall.SourcePos sourcePos) {
        if (node instanceof ExpressionParser.Node.Literal lit) return lit.value();

        if (node instanceof ExpressionParser.Node.Var v) {
            String name = normalizeName(v.name());
            return vars.get(player, name);
        }

        if (node instanceof ExpressionParser.Node.Call c) {
            String fn = normalizeName(c.name());
            if (functionRegistry == null || !functionRegistry.has(fn)) {
                return vars.get(player, fn);
            }

            List<String> args = new ArrayList<>();
            for (ExpressionParser.Node a : c.args()) {
                Object av = evalNode(player, a, sourcePos);
                args.add(av == null ? "null" : String.valueOf(av));
            }

            return functionRegistry.evaluate(
                FunctionCall.simple(fn, args, sourcePos),
                new FlowContext(player)
            );
        }

        if (node instanceof ExpressionParser.Node.Unary u) {
            Object r = evalNode(player, u.right(), sourcePos);
            return switch (u.type()) {
                case NOT -> !truthy(r);
                case SUBTRACT -> {
                    Double number = toNumber(r);
                    yield number == null ? 0.0 : -number;
                }
                default -> throw new IllegalStateException("Unsupported unary type: " + u.type());
            };
        }

        if (node instanceof ExpressionParser.Node.Binary binary) {
            Object left = evalNode(player, binary.left(), sourcePos);
            Object right = evalNode(player, binary.right(), sourcePos);

            return switch (binary.type()) {
                case OR -> truthy(left) || truthy(right);
                case AND -> truthy(left) && truthy(right);

                case EQUAL -> Objects.equals(stringify(left), stringify(right));
                case NOT_EQUAL -> !Objects.equals(stringify(left), stringify(right));

                case GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL -> compare(left, right, binary.type());

                case ADD, SUBTRACT, MULTIPLY, DIVIDE -> arithmetic(left, right, binary.type());

                default -> throw new IllegalStateException("Unsupported binary type: " + binary.type());
            };
        }

        throw new IllegalStateException("Unknown node type: " + node);
    }

    private static boolean compare(Object l, Object r, ExpressionTokenType op) {
        Double ld = toNumber(l);
        Double rd = toNumber(r);
        if (ld == null || rd == null) {
            return false;
        }

        return switch (op) {
            case GREATER -> ld > rd;
            case GREATER_OR_EQUAL -> ld >= rd;
            case LESS -> ld < rd;
            case LESS_OR_EQUAL -> ld <= rd;
            default -> throw new IllegalStateException("Not a compare type: " + op);
        };
    }

    private static Double toNumber(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) {
            try { return Double.parseDouble(s.trim()); } catch (Exception ignored) {}
        }
        if (v instanceof Boolean b) return b ? 1.0 : 0.0;
        return null;
    }

    private static boolean truthy(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.doubleValue() != 0.0;
        if (v instanceof String s) return !s.isBlank() && !"0".equals(s) && !"false".equalsIgnoreCase(s);
        return true;
    }

    private static String stringify(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static String normalizeName(String n) {
        if (n == null) return null;
        String s = n.trim();
        if (s.startsWith("$")) s = s.substring(1);
        return s;
    }

    public interface VarResolver {
        Object get(Entity player, String name);
    }

    private static Object arithmetic(Object left, Object right, ExpressionTokenType operand) {
        Double leftOperand = toNumber(left);
        Double rightOperand = toNumber(right);
        if (leftOperand == null || rightOperand == null) return 0.0;

        return switch (operand) {
            case ADD -> leftOperand + rightOperand;
            case SUBTRACT -> leftOperand - rightOperand;
            case MULTIPLY -> leftOperand * rightOperand;
            case DIVIDE -> (rightOperand == 0.0) ? 0.0 : (leftOperand / rightOperand);
            default -> throw new IllegalStateException("Not arithmetic operand: " + operand);
        };
    }
}
