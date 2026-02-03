package com.github.tilcob.game.skill.data;

import com.github.tilcob.game.stat.StatType;

import java.util.HashMap;
import java.util.Map;

public class SkillNodeDefinition {
    private String id;
    private String name;
    private String description;
    private int cost;
    private int requiredLevel;
    private String[] parentIds;
    private Map<String, Float> modifiers;

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

    public Map<String, Float> getModifiers() {
        return modifiers;
    }
}
