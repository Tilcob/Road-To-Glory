package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import java.util.HashMap;
import java.util.Map;

public class SkillComponent implements Component {
    public static final ComponentMapper<SkillComponent> MAPPER = ComponentMapper.getFor(SkillComponent.class);

    private final Map<String, SkillTreeState> trees = new HashMap<>();

    public Map<String, SkillTreeState> getTrees() {
        return trees;
    }

    public SkillTreeState getTreeState(String treeId) {
        return trees.computeIfAbsent(treeId, k -> new SkillTreeState());
    }
}
