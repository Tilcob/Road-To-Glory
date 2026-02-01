package com.github.tilcob.game.save.states;

import java.util.HashSet;
import java.util.Set;

public class SkillTreeStateSnapshot {
    private int currentLevel = 1;
    private int skillPoints = 0;
    private Set<String> unlockedNodes = new HashSet<>();

    public SkillTreeStateSnapshot() { }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public Set<String> getUnlockedNodes() {
        return Set.copyOf(unlockedNodes);
    }

    public void setUnlockedNodes(Set<String> unlockedNodes) {
        this.unlockedNodes = unlockedNodes == null ? new HashSet<>() : new HashSet<>(unlockedNodes);
    }
}
