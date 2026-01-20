package com.github.tilcob.game.item;

import com.github.tilcob.game.stat.StatKey;

import java.util.Map;

public record ItemDefinition(String id, String name, ItemCategory category, int maxStack, String icon,
                             Map<StatKey, Float> stats) {
    public ItemDefinition(String id, String name, ItemCategory category, int maxStack, String icon) {
        this(id, name, category, maxStack, icon, Map.of());
    }

    public ItemDefinition(String id, String name, ItemCategory category, int maxStack, String icon,
                          Map<StatKey, Float> stats) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.maxStack = maxStack;
        this.icon = icon;
        this.stats = stats == null ? Map.of() : Map.copyOf(stats);
    }

    public boolean isStackable() {
        return maxStack > 1;
    }
}
