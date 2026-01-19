package com.github.tilcob.game.quest;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String questId;
    private final String title;
    private final String description;
    private final QuestReward reward;
    private final int totalStages;
    private final List<String> stepJournals;
    private int currentStep = 0;
    private boolean rewardClaimed = false;
    private boolean completionNotified = false;

    public Quest(String questId, String title, String description, QuestReward reward) {
        this(questId, title, description, reward, 0, List.of());
    }

    public Quest(String questId, String title, String description, QuestReward reward, int totalStages) {
        this(questId, title, description, reward, totalStages, List.of());
    }

    public Quest(String questId, String title, String description, QuestReward reward, int totalStages, List<String> stepJournals) {
        this.questId = questId;
        this.title = title;
        this.description = description;
        this.reward = reward;
        this.totalStages = totalStages;
        this.stepJournals = stepJournals == null ? new ArrayList<>() : new ArrayList<>(stepJournals);
    }

    public String getQuestId() {
        return questId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public QuestReward getReward() {
        return reward;
    }

    public int getTotalStages() {
        return totalStages;
    }

    public List<String> getStepJournals() {
        return stepJournals;
    }

    public String getStepJournal(int index) {
        if (index < 0 || index >= stepJournals.size()) {
            return null;
        }
        String entry = stepJournals.get(index);
        return entry == null || entry.isBlank() ? null : entry;
    }

    public String getCurrentStepJournal() {
        return getStepJournal(currentStep);
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public void incCurrentStep() {
        currentStep++;
    }

    public boolean isCompleted() {
        return currentStep >= totalStages;
    }

    public boolean isCompletionNotified() {
        return completionNotified;
    }

    public void setCompletionNotified(boolean completionNotified) {
        this.completionNotified = completionNotified;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }
}
