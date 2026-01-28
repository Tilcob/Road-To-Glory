package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogNode;
import com.github.tilcob.game.flow.*;

import java.util.*;

public class QuestYarnRuntime {
    private static final String TAG = QuestYarnRuntime.class.getSimpleName();

    private final YarnRuntime runtime;
    private final Map<Entity, Map<String, Object>> variables;
    private final Map<String, Object> defaultVariables;
    private final Map<String, DialogData> allDialogs;
    private final Map<String, DialogData> allQuestDialogs;
    private final CommandRegistry commandRegistry;
    private final FunctionRegistry functionRegistry;
    private final FlowExecutor flowExecutor;

    public QuestYarnRuntime(YarnRuntime runtime,
                            Map<String, DialogData> allDialogs,
                            Map<String, DialogData> allQuestDialogs,
                            CommandRegistry commandRegistry,
                            FlowExecutor flowExecutor,
                            FunctionRegistry functionRegistry) {
        this.runtime = runtime;
        this.variables = new HashMap<>();
        this.defaultVariables = new HashMap<>();
        this.allDialogs = allDialogs == null ? Collections.emptyMap() : allDialogs;
        this.allQuestDialogs = allQuestDialogs == null ? Collections.emptyMap() : allQuestDialogs;
        this.commandRegistry = commandRegistry;
        this.functionRegistry = functionRegistry;
        this.flowExecutor = flowExecutor;
    }

    public void executeStartNode(Entity player, String startNode) {
        if (startNode == null || startNode.isBlank()) return;
        if (YarnRuntime.isCommandLine(startNode.trim())) {
            tryExecuteCommandLine(player, startNode, new CommandCall.SourcePos("quest", startNode, 0));
            return;
        }
        executeNode(player, startNode);
    }

    public boolean executeNode(Entity player, String nodeId) {
        DialogNode node = findNode(nodeId, true);
        if (node == null) return false;
        Array<String> lines = node.lines();
        if (lines == null) return false;
        boolean shouldExecute = true;
        for (int i = 0; i < lines.size; i++) {
            String line = lines.get(i);
            String trimmed = line == null ? "" : line.trim();
            if (isIfLine(trimmed)) {
                shouldExecute = evaluateCondition(player, trimmed);
                continue;
            }
            if (isEndIfLine(trimmed)) {
                shouldExecute = true;
                continue;
            }
            if (!shouldExecute) continue;
            String trimmedLine = line == null ? "" : line.trim();
            if (tryExecuteCommandLine(player, trimmedLine, new CommandCall.SourcePos("quest", nodeId, i))) {
                continue;
            }
        }
        return true;
    }

    public void executeCommandLine(Entity player, String line) {
        tryExecuteCommandLine(player, line, new CommandCall.SourcePos("quest", "inline", -1));
    }

    public void setVariable(Entity player, String name, Object value) {
        if (name == null || name.isBlank()) return;
        getVariablesFor(player, true).put(name, value);
    }

    public Object getVariable(Entity player, String name) {
        if (name == null || name.isBlank()) return null;
        Map<String, Object> scoped = getVariablesFor(player, false);
        return scoped == null ? null : scoped.get(name);
    }

    public boolean hasNode(String nodeId) {
        return findNode(nodeId, false) != null;
    }

    private boolean isIfLine(String line) {
        return line.startsWith("<<if ") && line.endsWith(">>");
    }

    private boolean isEndIfLine(String line) {
        return line.equals("<<endif>>");
    }

    private boolean evaluateCondition(Entity player, String line) {
        String inner = line.substring(2, line.length() - 2).trim();
        String condition = inner.substring("if ".length()).trim();
        String[] parts = condition.split("==", 2);

        if (parts.length != 2) return false;
        String left = parts[0].trim();
        String right = parts[1].trim();

        if (right.startsWith("\"") && right.endsWith("\"") && right.length() >= 2) {
            right = right.substring(1, right.length() - 1);
        }

        Object value = evaluateLeft(player, left, new CommandCall.SourcePos("quest", "condition", -1));
        return value != null && right.equals(String.valueOf(value));
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

    private Map<String, Object> getVariablesFor(Entity player, boolean create) {
        if (player == null) return defaultVariables;
        Map<String, Object> scoped = variables.get(player);
        if (scoped == null && create) {
            scoped = new HashMap<>();
            variables.put(player, scoped);
        }
        return scoped;
    }

    private DialogNode findNode(String nodeId, boolean logMissing) {
        if (nodeId == null || nodeId.isBlank()) return null;
        DialogNode node = getDialogNode(allDialogs, nodeId);

        if (node != null) return node;
        node = getDialogNode(allQuestDialogs, nodeId);
        if (node != null) return node;
        if (logMissing && Gdx.app != null) {
            Gdx.app.debug(TAG, "Quest Yarn node not found: " + nodeId);
        }
        return null;
    }

    private DialogNode getDialogNode(Map<String, DialogData> dialogDataMap, String nodeId) {
        for (DialogData dialogData : dialogDataMap.values()) {
            if (dialogData == null) continue;
            ObjectMap<String, DialogNode> nodes = dialogData.nodesById();
            if (nodes == null || nodes.isEmpty()) continue;
            DialogNode node = nodes.get(nodeId);
            if (node != null) return node;
        }
        return null;
    }

    private boolean tryExecuteCommandLine(Entity player, String line, CommandCall.SourcePos source) {
        Optional<CommandCall> callOptional = runtime.parseCommandLine(line, source);
        if (callOptional.isEmpty()) return false;

        List<FlowAction> actions = commandRegistry.dispatch(callOptional.get(), new FlowContext(player));
        flowExecutor.execute(actions);
        return true;
    }
}
