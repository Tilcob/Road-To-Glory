package com.github.tilcob.game.save.states.quest;

import com.github.tilcob.game.quest.Quest;

import java.util.HashMap;
import java.util.Map;

public class QuestState {
    private String questId;
    private boolean active;
    private boolean completed;
    private int stage;
    private boolean rewardClaimed;

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
}
