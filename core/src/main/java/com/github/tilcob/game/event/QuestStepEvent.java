package com.github.tilcob.game.event;

public record QuestStepEvent(Type type, String target) {
    public enum Type {
        TALK,
    }
}
