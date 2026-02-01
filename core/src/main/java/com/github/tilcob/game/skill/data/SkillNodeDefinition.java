package com.github.tilcob.game.skill.data;

import com.github.tilcob.game.stat.StatType;

import java.util.Map;

public class SkillNodeDefinition {
    private String id;
    private String name;
    private String description;
    private int cost;
    private int requiredLevel;
    private String[] parentIds;
    private Map<StatType, Float> modifiers;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public String[] getParentIds() {
        return parentIds;
    }

    public Map<StatType, Float> getModifiers() {
        return modifiers;
    }
}
