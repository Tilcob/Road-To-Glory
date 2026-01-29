package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.*;

import java.util.*;

public abstract class BaseYarnRuntime {
    protected final YarnRuntime runtime;
    protected final CommandRegistry commandRegistry;
    protected final FunctionRegistry functionRegistry;
    protected final FlowExecutor flowExecutor;

    private final Map<Entity, Map<String, Object>> variables = new HashMap<>();
    private final Map<String, Object> defaultVariables = new HashMap<>();

    protected BaseYarnRuntime(YarnRuntime runtime,
                              CommandRegistry commandRegistry,
                              FlowExecutor flowExecutor,
                              FunctionRegistry functionRegistry) {
        this.runtime = runtime;
        this.commandRegistry = commandRegistry;
        this.flowExecutor = flowExecutor;
        this.functionRegistry = functionRegistry;
    }

    public boolean tryExecuteCommandLine(Entity player, String line) {
        return tryExecuteCommandLine(player, line, CommandCall.SourcePos.unknown());
    }

    public boolean tryExecuteCommandLine(Entity player, String line, CommandCall.SourcePos source) {
        Optional<CommandCall> callOptional = runtime.parseCommandLine(line, source);
        if (callOptional.isEmpty()) return false;

        List<FlowAction> actions = commandRegistry.dispatch(callOptional.get(), new FlowContext(player));
        flowExecutor.execute(actions);
        return true;
    }

    public void executeCommandLine(Entity player, String line) {
        executeCommandLine(player, line, CommandCall.SourcePos.unknown());
    }

    public void executeCommandLine(Entity player, String line, CommandCall.SourcePos source) {
        if (!tryExecuteCommandLine(player, line, source)) {
            throw new IllegalStateException("Not a Yarn command line: '" + line + "' @ " + source);
        }
    }

    public void setVariable(Entity player, String name, Object value) {
        String key = normalizeName(name);
        if (key == null || key.isBlank()) return;
        Map<String, Object> scoped = getVariablesFor(player, true);
        scoped.put(key, value);
    }

    public Object getVariable(Entity player, String name) {
        String key = normalizeName(name);
        if (key == null || key.isBlank()) return null;
        Map<String, Object> scoped = getVariablesFor(player, false);
        return scoped == null ? null : scoped.get(key);
    }

    private static String normalizeName(String name) {
        if (name == null) return null;
        String n = name.trim();
        if (n.startsWith("$")) n = n.substring(1);
        return n;
    }

    protected boolean evaluateCondition(Entity player, String condition, CommandCall.SourcePos source) {
        if (condition == null) return false;
        String expr = condition.trim();
        if (expr.isEmpty()) return false;

        // Shorthand: treat expression as boolean/truthy check:
        //   <<if $has_flag guard_angry>>
        //   <<if someVar>>
        if (!expr.contains("==") && !expr.contains("!=")) {
            Object v = evalValue(player, expr, source);
            return truthy(v);
        }

        boolean notEquals = expr.contains("!=");
        String[] parts = expr.split(notEquals ? "!=" : "==", 2);
        if (parts.length != 2) return false;

        Object leftValue = evalValue(player, parts[0].trim(), source);
        Object rightValue = evalValue(player, parts[1].trim(), source);

        boolean eq = Objects.equals(stringify(leftValue), stringify(rightValue));
        return notEquals ? !eq : eq;
    }

    private Object evalValue(Entity player, String token, CommandCall.SourcePos source) {
        if (token == null) return null;
        String t = token.trim();
        if (t.isEmpty()) return null;

        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            return t.substring(1, t.length() - 1);
        }
        if ("true".equalsIgnoreCase(t)) return true;
        if ("false".equalsIgnoreCase(t)) return false;

        if (t.matches("-?\\d+")) {
            try { return Integer.parseInt(t); } catch (NumberFormatException ignored) {}
        }
        if (t.matches("-?\\d+\\.\\d+")) {
            try { return Float.parseFloat(t); } catch (NumberFormatException ignored) {}
        }

        String expr = t.startsWith("$") ? t.substring(1) : t;
        String[] tokens = expr.split("\\s+");
        String head = tokens[0];

        if (functionRegistry != null && functionRegistry.has(head)) {
            List<String> args = tokens.length > 1
                ? List.of(Arrays.copyOfRange(tokens, 1, tokens.length))
                : List.of();
            return functionRegistry.evaluate(FunctionCall.simple(head, args, source), new FlowContext(player));
        }

        return getVariable(player, expr);
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

    private Map<String, Object> getVariablesFor(Entity player, boolean create) {
        if (player == null) return defaultVariables;

        Map<String, Object> scoped = variables.get(player);
        if (scoped == null && create) {
            scoped = new HashMap<>();
            variables.put(player, scoped);
        }
        return scoped;
    }
}
