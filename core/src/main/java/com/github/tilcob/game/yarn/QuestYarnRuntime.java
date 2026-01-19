package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;

public class QuestYarnRuntime {
    private final YarnRuntime runtime;

    public QuestYarnRuntime(QuestYarnBridge bridge) {
        this.runtime = new YarnRuntime();
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
}
