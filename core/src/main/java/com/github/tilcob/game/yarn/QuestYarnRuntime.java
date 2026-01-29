package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.CommandCall;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowExecutor;
import com.github.tilcob.game.flow.FunctionRegistry;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.yarn.script.ScriptEvent;

import java.util.List;

public class QuestYarnRuntime extends BaseYarnRuntime {
    private final IfStack ifStack = new IfStack();
    private final QuestScriptStore scriptStore;

    public QuestYarnRuntime(YarnRuntime runtime,
                            QuestYarnRegistry questYarnRegistry,
                            CommandRegistry commandRegistry,
                            FlowExecutor flowExecutor,
                            FunctionRegistry functionRegistry) {
        super(runtime, commandRegistry, flowExecutor, functionRegistry);
        this.scriptStore = new QuestScriptStore(questYarnRegistry);
    }

    public void executeStartNode(Entity player, String startNodeId) {
        executeNode(player, startNodeId);
    }

    public boolean hasNode(String nodeId) {
        return scriptStore.hasNode(nodeId);
    }

    public boolean executeNode(Entity player, String nodeId) {
        List<ScriptEvent> events = scriptStore.get(nodeId);
        if (events == null) return false;

        runCompiled(player, events);
        return true;
    }

    public void run(Entity player, List<String> lines) {
        ifStack.clear();
        for (String line : lines) {
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.startsWith("<<if ")) {
                String expr = trimmed;
                if (expr.startsWith("<<") && expr.endsWith(">>")) {
                    expr = expr.substring(2, expr.length() - 2).trim();
                    if (expr.startsWith("if")) expr = expr.substring(2).trim();
                }
                boolean r = evaluateCondition(player, expr, CommandCall.SourcePos.unknown());
                ifStack.onIfStart(r);
                continue;
            }

            if (trimmed.startsWith("<<else")) {
                ifStack.onElse();
                continue;
            }

            if (trimmed.startsWith("<<endif")) {
                ifStack.onEndIf();
                continue;
            }

            if (!ifStack.isExecuting()) continue;

            tryExecuteCommandLine(player, trimmed, CommandCall.SourcePos.unknown());
        }
    }

    private void runCompiled(Entity player, List<ScriptEvent> events) {
        ifStack.clear();

        for (ScriptEvent e : events) {
            if (e instanceof ScriptEvent.IfStart ifs) {
                boolean r = evaluateCondition(player, ifs.condition(), CommandCall.SourcePos.unknown());
                ifStack.onIfStart(r);
                continue;
            }
            if (e instanceof ScriptEvent.Else) {
                ifStack.onElse();
                continue;
            }
            if (e instanceof ScriptEvent.EndIf) {
                ifStack.onEndIf();
                continue;
            }

            if (!ifStack.isExecuting()) continue;

            if (e instanceof ScriptEvent.Command cmd) {
                // cmd.raw already is "<<...>>"
                tryExecuteCommandLine(player, cmd.raw(), CommandCall.SourcePos.unknown());
            }
        }
    }
}
