package com.github.tilcob.game.save.states.quest;

import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.step.QuestStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestState {
    private String questId;
    private boolean active;
    private boolean completed;
    private int stage;
    private boolean rewardClaimed;
    private Map<String, Boolean> flags = new HashMap<>();
    private Map<String, Integer> counters = new HashMap<>();

    public QuestState() {}

    public QuestState(Quest quest) {
        this.questId = quest.getQuestId();
        this.active = true;
        this.completed = quest.isCompleted();
        this.stage = quest.getCurrentStep();
        this.rewardClaimed = quest.isRewardClaimed();
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Boolean> flags) {
        this.flags = flags == null ? new HashMap<>() : flags;
    }

    public Map<String, Integer> getCounters() {
        return counters;
    }

    public void setCounters(Map<String, Integer> counters) {
        this.counters = counters == null ? new HashMap<>() : counters;
    }
}
