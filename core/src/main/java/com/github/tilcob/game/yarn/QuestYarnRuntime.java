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
    private String nodeId;
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
        this.nodeId = nodeId;
        if (events == null) return false;

        runCompiled(player, events);
        return true;
    }

    private void runCompiled(Entity player, List<ScriptEvent> events) {
        ifStack.clear();

        for (int i = 0; i < events.size(); i++) {
            ScriptEvent e = events.get(i);
            CommandCall.SourcePos pos = new CommandCall.SourcePos("quests", nodeId, i);
            if (e instanceof ScriptEvent.IfStart ifs) {
                boolean r = evaluateCondition(player, ifs.condition(), pos);
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
                tryExecuteCommandLine(player, cmd.raw(), pos);
            }
        }
    }

    public String getQuestSignalNodeId(String questId, String eventType) {
        return scriptStore.getQuestSignalNodeId(questId, eventType);
    }
}
