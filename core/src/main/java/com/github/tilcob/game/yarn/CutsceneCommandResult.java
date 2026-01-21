package com.github.tilcob.game.yarn;

public record CutsceneCommandResult(boolean handled, float waitSeconds, boolean waitForDialog) {
    public static CutsceneCommandResult notCommand() {
        return new CutsceneCommandResult(false, 0f, false);
    }
}
