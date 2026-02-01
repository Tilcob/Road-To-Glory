package com.github.tilcob.game.skill.data;

import java.util.List;

public class SkillTreeDefinition {
    private String id;
    private String name;
    private String description;
    private int[] xpTable;
    private List<SkillNodeDefinition> nodes;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int[] getXpTable() {
        return xpTable;
    }

    public List<SkillNodeDefinition> getNodes() {
        return nodes;
    }
}
