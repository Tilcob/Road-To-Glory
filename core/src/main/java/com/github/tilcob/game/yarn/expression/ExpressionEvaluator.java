package com.github.tilcob.game.yarn.expression;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.CommandCall;
import com.github.tilcob.game.flow.FlowContext;
import com.github.tilcob.game.flow.FunctionCall;
import com.github.tilcob.game.flow.FunctionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExpressionEvaluator {
    private final FunctionRegistry functionRegistry;
    private final VarResolver vars;
    private final ConcurrentMap<String, ExpressionParser.Node> cache = new ConcurrentHashMap<>();

    public ExpressionEvaluator(FunctionRegistry functionRegistry, VarResolver vars) {
        this.functionRegistry = functionRegistry;
        this.vars = vars;
    }

    public boolean evalBool(Entity player, String expr, CommandCall.SourcePos sourcePos) {
        Object evaluationResult = eval(player, expr, sourcePos);
        return truthy(evaluationResult);
    }

    public Object eval(Entity player, String expr, CommandCall.SourcePos sourcePos) {
        String key = expr == null ? "" : expr.trim();
        if (key.isEmpty()) return false;

        try {
            ExpressionParser.Node parsed = cache.computeIfAbsent(key, k -> {
                List<ExpressionToken> tokens = new ExpressionLexer(k, CommandCall.SourcePos.unknown()).lex();
                return new ExpressionParser(tokens, k, CommandCall.SourcePos.unknown()).parse();
            });
            return evalNode(player, parsed, sourcePos);
        } catch (YarnExpressionException ex) {
            throw new YarnExpressionException(
                sourcePos == null ? CommandCall.SourcePos.unknown() : sourcePos,
                ex.expression(),
                ex.expressionPos(),
                ex.getMessage()
            );
        } catch (Exception ex) {
            throw new YarnExpressionException(
                sourcePos == null ? CommandCall.SourcePos.unknown() : sourcePos,
                expr,
                0,
                "Evaluation error: " + ex.getMessage()
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
            if (binary.type() == ExpressionTokenType.OR) {
                Object left = evalNode(player, binary.left(), sourcePos);
                if (truthy(left)) return true;
                Object right = evalNode(player, binary.right(), sourcePos);
                return truthy(right);
            }

            if (binary.type() == ExpressionTokenType.AND) {
                Object left = evalNode(player, binary.left(), sourcePos);
                if (!truthy(left)) return false;
                Object right = evalNode(player, binary.right(), sourcePos);
                return truthy(right);
            }

            Object left = evalNode(player, binary.left(), sourcePos);
            Object right = evalNode(player, binary.right(), sourcePos);

            return switch (binary.type()) {
                case EQUAL -> equalsOp(left, right);
                case NOT_EQUAL -> !equalsOp(left, right);
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

    private static boolean equalsOp(Object left, Object right) {
        if (left == null || right == null) return Objects.equals(left, right);

        if (left instanceof Number || right instanceof Number) {
            Double leftDouble = toNumber(left);
            Double rightDouble = toNumber(right);
            if (leftDouble != null && rightDouble != null) return Double.compare(leftDouble, rightDouble) == 0;
            return false;
        }
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return leftBoolean == rightBoolean;
        }
        if (left instanceof Boolean || right instanceof Boolean) {
            Boolean leftBoolean = toBoolean(left);
            Boolean rightBoolean = toBoolean(right);
            if (leftBoolean != null && rightBoolean != null) return leftBoolean == rightBoolean;
            return false;
        }

        if (left instanceof String ls && right instanceof String rs) return ls.equals(rs);
        return Objects.equals(String.valueOf(left), String.valueOf(right));
    }

    private static Boolean toBoolean(Object v) {
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) {
            String t = s.trim();
            if (t.equalsIgnoreCase("true")) return true;
            if (t.equalsIgnoreCase("false")) return false;
        }
        return null;
    }

    private static Double toNumber(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) {
            try { return Double.parseDouble(s.trim()); } catch (Exception ignored) {}
        }
        return null;
    }

    private static boolean truthy(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.doubleValue() != 0.0;
        if (v instanceof String s) return !s.isBlank() && !"0".equals(s) && !"false".equalsIgnoreCase(s);
        return true;
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
