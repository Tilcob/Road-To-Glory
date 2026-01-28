package com.github.tilcob.game.flow.commands;


public record CutsceneCommandResult(boolean isCommand, Wait waitTime) {


    public static CutsceneCommandResult notACommand() {
        return new CutsceneCommandResult(false, new Wait.None());
    }
    public static CutsceneCommandResult commandExecuted() {
        return new CutsceneCommandResult(true, new Wait.None());
    }
    public static CutsceneCommandResult waitSeconds(float seconds) {
        return new CutsceneCommandResult(true, new Wait.Seconds(seconds));
    }
    public static CutsceneCommandResult waitForCamera() {
        return new CutsceneCommandResult(true, new Wait.Camera());
    }
    public static CutsceneCommandResult waitForMove() {
        return new CutsceneCommandResult(true, new Wait.Move());
    }
    public static CutsceneCommandResult waitForDialog() {
        return new CutsceneCommandResult(true, new Wait.Dialog());
    }


    public sealed interface Wait permits Wait.None, Wait.Seconds, Wait.Camera, Wait.Move, Wait.Dialog {
        record None() implements Wait {}
        record Seconds(float seconds) implements Wait {}
        record Camera() implements Wait {}
        record Move() implements Wait {}
        record Dialog() implements Wait {}
    }
}
