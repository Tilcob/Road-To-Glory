package com.github.tilcob.game.item;

import com.github.tilcob.game.stat.StatKey;
import com.github.tilcob.game.stat.StatType;

import java.util.List;
import java.util.Map;

public record ItemDefinition(String id, String name, ItemCategory category, int maxStack, String icon,
                             Map<StatKey, Float> stats, List<ItemStatModifier> statModifiers,
                             Map<StatType, Float> requirements) {

    public ItemDefinition(String id, String name, ItemCategory category, int maxStack, String icon) {
        this(id, name, category, maxStack, icon, Map.of(), List.of(), Map.of());
    }

    public ItemDefinition(String id, String name, ItemCategory category, int maxStack, String icon,
                          Map<StatKey, Float> stats, List<ItemStatModifier> statModifiers,
                          Map<StatType, Float> requirements) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.maxStack = maxStack;
        this.icon = icon;
        this.stats = stats == null ? Map.of() : Map.copyOf(stats);
        this.statModifiers = statModifiers == null ? List.of() : List.copyOf(statModifiers);
        this.requirements = requirements == null ? Map.of() : Map.copyOf(requirements);
    }

    public boolean isStackable() {
        return maxStack > 1;
    }
}
