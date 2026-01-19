package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;

@FunctionalInterface
public interface YarnCommandRegistry {
    void register(String command, CommandHandler handler);

    @FunctionalInterface
    interface CommandHandler {
        void handle(Entity player, String[] args);
    }
}
