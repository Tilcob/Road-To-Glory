package com.github.tilcob.game.system;

public enum SystemOrder {
    INPUT(0),
    AI(10),
    PHYSICS(20),
    COMBAT(30),
    GAMEPLAY(40),
    RENDER(50),
    DEBUG_RENDER(60);

    private final int priority;

    SystemOrder(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
