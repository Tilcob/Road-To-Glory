package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;

public class CutsceneYarnRuntime {
    private static final String TAG = CutsceneYarnRuntime.class.getSimpleName();

    private final YarnRuntime runtime;

    public CutsceneYarnRuntime(CutsceneYarnBridge bridge) {
        this.runtime = new YarnRuntime();
        bridge.registerAll(runtime);
    }

    public CutsceneCommandResult executeCommandLine(Entity player, String line) {
        if (line == null) {
            return CutsceneCommandResult.notCommand();
        }
        String trimmed = line.trim();
        if (!YarnRuntime.isCommandLine(trimmed)) {
            return CutsceneCommandResult.notCommand();
        }
        String inner = trimmed.substring(2, trimmed.length() - 2).trim();
        if (inner.isEmpty()) {
            return new CutsceneCommandResult(true, 0f, false, false, false);
        }
        String[] parts = inner.split("\\s+");
        String command = parts[0];
        String[] args = argsFrom(parts);
        if ("wait".equalsIgnoreCase(command)) {
            float waitSeconds = args.length > 0 ? parseFloat(args[0], 0f) : 0f;
            return new CutsceneCommandResult(true, Math.max(0f, waitSeconds), false, false, false);
        }
        if ("wait_for_camera".equalsIgnoreCase(command)) {
            return new CutsceneCommandResult(true, 0f, false, true, false);
        }
        if ("wait_for_move".equalsIgnoreCase(command)) {
            return new CutsceneCommandResult(true, 0f, false, false, true);
        }
        boolean handled = runtime.executeCommand(command, player, args);
        if (!handled && Gdx.app != null) {
            Gdx.app.debug(TAG, "Unhandled cutscene command: " + command);
        }
        boolean waitForDialog = "start_dialog".equalsIgnoreCase(command);
        return new CutsceneCommandResult(true, 0f, waitForDialog, false, false);
    }

    private static String[] argsFrom(String[] parts) {
        if (parts.length <= 1) {
            return new String[0];
        }
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }

    private float parseFloat(String raw, float fallback) {
        try {
            return Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
