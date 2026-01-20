package com.github.tilcob.game.stat;

public enum StatType {
    ATTACK("attack"),
    BURN_TIME("burn_time"),
    DAMAGE("damage"),
    LIFE_REGENERATION("life_regeneration"),
    MAX_LIFE("max_life"),;

    private final String id;

    StatType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
