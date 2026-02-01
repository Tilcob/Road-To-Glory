package com.github.tilcob.game.save.states;

import java.util.HashSet;
import java.util.Set;

public class SkillTreeState {
    private int currentLevel = 1;
    private int currentXp = 0;
    private int skillPoints = 0;
    private Set<String> unlockedNodes = new HashSet<>();

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getCurrentXp() {
        return currentXp;
    }

    public void setCurrentXp(int currentXp) {
        this.currentXp = currentXp;
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

    public void addUnlockedNode(String nodeId) {
        this.unlockedNodes.add(nodeId);
    }

    public boolean isUnlocked(String nodeId) {
        return this.unlockedNodes.contains(nodeId);
    }
}
