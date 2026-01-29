package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.*;

import java.util.*;

public class DialogYarnRuntime {
    private final YarnRuntime runtime;
    private final CommandRegistry commandRegistry;
    private final FunctionRegistry functionRegistry;
    private final FlowExecutor flowExecutor;

    public DialogYarnRuntime(YarnRuntime runtime,
                             CommandRegistry commandRegistry,
                             FlowExecutor flowExecutor,
                             FunctionRegistry functionRegistry) {
        this.runtime = runtime;
        this.commandRegistry = commandRegistry;
        this.flowExecutor = flowExecutor;
        this.functionRegistry = functionRegistry;
    }

    public boolean tryExecuteCommandLine(Entity player, String line, CommandCall.SourcePos source) {
        Optional<CommandCall> callOptional = runtime.parseCommandLine(line, source);
        if (callOptional.isEmpty()) return false;

        CommandCall call = callOptional.get();
        List<FlowAction> actions = commandRegistry.dispatch(call, new FlowContext(player));
        return true;
    }

    public boolean tryExecuteCommandLine(Entity player, String line) {
        return tryExecuteCommandLine(player, line, CommandCall.SourcePos.unknown());
    }

    public boolean evaluateCondition(Entity player, String condition, CommandCall.SourcePos source) {
        String[] parts = condition.split("==", 2);
        if (parts.length != 2) return false;

        String left = parts[0].trim();
        String right = parts[1].trim();

        if (right.startsWith("\"") && right.endsWith("\"") && right.length() >= 2) {
            right = right.substring(1, right.length() - 1);
        }

        Object leftValue = evaluateLeft(player, left, source);
        return leftValue != null && right.equals(String.valueOf(leftValue));
    }

    private Object evaluateLeft(Entity player, String leftExpr, CommandCall.SourcePos source) {
        if (leftExpr == null || leftExpr.isBlank()) return null;
        leftExpr = leftExpr.trim();
        if (!leftExpr.startsWith("$")) return getVariable(player, leftExpr);

        String[] tokens = leftExpr.split("\\s+");
        String functionName = tokens[0];

        if (functionRegistry.has(functionName)) {
            List<String> arguments = tokens.length > 1
                ? List.of(Arrays.copyOfRange(tokens, 1, tokens.length))
                : List.of();
            return functionRegistry.evaluate(
                FunctionCall.simple(functionName, arguments, source),
                new FlowContext(player)
            );
        }
        return getVariable(player, functionName);
    }

    public Object getVariable(Entity player, String name) {
        if (name == null || name.isBlank()) return null;
        Map<String, Object> scoped = getVariablesFor(player, false);
        return scoped == null ? null : scoped.get(name);
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
