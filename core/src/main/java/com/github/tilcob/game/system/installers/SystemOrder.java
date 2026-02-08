package com.github.tilcob.game.system.installers;

public enum SystemOrder {
    INPUT(0),
    AI(10),
    PHYSICS(20),
    COMBAT(30),
    COMBAT_ATTACK(31),
    COMBAT_HIT(32),
    COMBAT_DAMAGE(33),
    COMBAT_LIFE(34),
    COMBAT_DEATH(35),
    COMBAT_GAME_OVER(36),
    COMBAT_TRIGGER(37),
    GAMEPLAY(40),
    RENDER(50),
    RENDER_OVERLAY(55),
    DEBUG_RENDER(60);

    private final int priority;

    SystemOrder(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
