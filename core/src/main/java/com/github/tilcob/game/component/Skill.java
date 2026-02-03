package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.save.states.SkillTreeState;

import java.util.HashMap;
import java.util.Map;

public class Skill implements Component {
    public static final ComponentMapper<Skill> MAPPER = ComponentMapper.getFor(Skill.class);

    private final Map<String, SkillTreeState> trees = new HashMap<>();
    private int sharedSkillPoints = 0;

    public Map<String, SkillTreeState> getTrees() {
        return trees;
    }

    public SkillTreeState getTreeState(String treeId) {
        return trees.computeIfAbsent(treeId, k -> new SkillTreeState());
    }

    public int getSharedSkillPoints() {
        return sharedSkillPoints;
    }

    public void setSharedSkillPoints(int sharedSkillPoints) {
        this.sharedSkillPoints = Math.max(0, sharedSkillPoints);
    }

    public void addSharedSkillPoints(int amount) {
        if (amount <= 0) return;
        setSharedSkillPoints(sharedSkillPoints + amount);
    }

    public boolean spendSharedSkillPoints(int amount) {
        if (amount <= 0 || sharedSkillPoints < amount) return false;
        sharedSkillPoints -= amount;
        return true;
    }
}
