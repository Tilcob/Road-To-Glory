package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;

@FunctionalInterface
public interface YarnFunctionRegistry {
    void register(String function, FunctionHandler handler);

    @FunctionalInterface
    interface FunctionHandler {
        Object evaluate(Entity player, String[] args);
    }
}
