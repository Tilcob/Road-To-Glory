package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;

import java.util.HashMap;
import java.util.Map;

public class QuestYarnRuntime {
    private final YarnRuntime runtime;
    private final Map<String, Object> variables;

    public QuestYarnRuntime(QuestYarnBridge bridge) {
        this.runtime = new YarnRuntime();
        this.variables = new HashMap<>();
        bridge.registerAll(runtime);
    }

    public void executeStartNode(Entity player, String startNode) {
        if (startNode == null || startNode.isBlank()) {
            return;
        }
        if (YarnRuntime.isCommandLine(startNode.trim())) {
            runtime.executeCommandLine(player, startNode);
            return;
        }
        // TODO: Execute quest yarn nodes via the Yarn Spinner runtime once node lookup is wired in.
    }

    public boolean executeCommandLine(Entity player, String line) {
        return runtime.executeCommandLine(player, line);
    }

    public void setVariable(String name, Object value) {
        if (name == null || name.isBlank()) {
            return;
        }
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }
}
