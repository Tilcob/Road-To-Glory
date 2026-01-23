package com.github.tilcob.game.stat;

import java.util.HashMap;
import java.util.Map;

public enum StatType {
    ATTACK("attack"),
    BURN_TIME("burn_time"),
    DAMAGE("damage"),
    LIFE_REGENERATION("life_regeneration"),
    MAX_LIFE("max_life"),
    STAMINA("stamina"),
    STRENGTH("strength"),
    PROTECTION("protection"),;

    private final String id;
    private static final Map<String, StatType> BY_ID = new HashMap<>();

    static {
        for (StatType type : StatType.values()) {
            BY_ID.put(type.getId(), type);
        }
    }

    StatType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static StatType fromId(String id) {
        if (id == null) return null;
        return BY_ID.get(id);
    }
}
