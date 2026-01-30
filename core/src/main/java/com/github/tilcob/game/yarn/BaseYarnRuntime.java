package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.*;
import com.github.tilcob.game.yarn.expression.ExpressionEvaluator;
import com.github.tilcob.game.yarn.expression.YarnExpressionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseYarnRuntime {
    protected final YarnRuntime runtime;
    protected final CommandRegistry commandRegistry;
    protected final FunctionRegistry functionRegistry;
    protected final FlowExecutor flowExecutor;
    private final boolean strictYarnErrors;

    private final Map<Entity, Map<String, Object>> variables = new HashMap<>();
    private final Map<String, Object> defaultVariables = new HashMap<>();

    protected BaseYarnRuntime(YarnRuntime runtime,
                              CommandRegistry commandRegistry,
                              FlowExecutor flowExecutor,
                              FunctionRegistry functionRegistry) {
        this(runtime, commandRegistry, flowExecutor, functionRegistry, false);
    }

    protected BaseYarnRuntime(YarnRuntime runtime,
                              CommandRegistry commandRegistry,
                              FlowExecutor flowExecutor,
                              FunctionRegistry functionRegistry,
                              boolean strictYarnErrors) {
        this.runtime = runtime;
        this.commandRegistry = commandRegistry;
        this.flowExecutor = flowExecutor;
        this.functionRegistry = functionRegistry;
        this.strictYarnErrors = strictYarnErrors;
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

        try {
            var evaluator = new ExpressionEvaluator(
                functionRegistry,
                this::getVariable
            );
            return evaluator.evalBool(player, expr, source);
        } catch (YarnExpressionException ex) {
            String message = formatExpressionError(ex);
            logYarnError(message);
            if (strictYarnErrors) throw ex;
            return false;
        }
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

    private static void logYarnError(String msg) {
        try {
            com.badlogic.gdx.Gdx.app.error("Yarn", msg);
        } catch (Exception ignored) {
            System.err.println("[Yarn] " + msg);
        }
    }

    private static String formatExpressionError(YarnExpressionException ex) {
        String expr = ex.expression();
        int pos = Math.max(0, Math.min(ex.expressionPos(), expr.length()));

        int start = Math.max(0, pos - 30);
        int end = Math.min(expr.length(), pos + 30);
        String snippet = expr.substring(start, end);
        String caret = " ".repeat(Math.max(0, pos - start)) + '^';
        String where = ex.source() == null ? "unknown" : ex.source().toString();

        return where + "\n"
            + "If-expression error: " + ex.getMessage() + "\n"
            + "Expr: " + snippet + "\n"
            + "      " + caret + "\n";
    }
}
