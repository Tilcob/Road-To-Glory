package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import java.util.HashMap;
import java.util.Map;

public class Skill implements Component {
    public static final ComponentMapper<Skill> MAPPER = ComponentMapper.getFor(Skill.class);

    private final Map<String, SkillTreeState> trees = new HashMap<>();

    public Map<String, SkillTreeState> getTrees() {
        return trees;
    }

    public SkillTreeState getTreeState(String treeId) {
        return trees.computeIfAbsent(treeId, k -> new SkillTreeState());
    }
}
