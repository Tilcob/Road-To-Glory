package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;

public class DialogYarnRuntime {
    private final YarnRuntime runtime;

    public DialogYarnRuntime(DialogYarnBridge bridge) {
        this.runtime = new YarnRuntime();
        bridge.registerAll(runtime);
    }

    public boolean executeCommandLine(Entity player, String line) {
        return runtime.executeCommandLine(player, line);
    }
}
