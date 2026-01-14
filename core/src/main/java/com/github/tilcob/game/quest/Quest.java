package com.github.tilcob.game.quest;

import com.github.tilcob.game.quest.step.QuestStep;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String questId;
    private final String title;
    private final String description;
    private final QuestReward reward;
    private final List<QuestStep> steps = new ArrayList<>();
    private int currentStep = 0;

    public Quest(String questId, String title, String description) {
        this(questId, title, description, null);
    }

    public Quest(String questId, String title, String description, QuestReward reward) {
        this.questId = questId;
        this.title = title;
        this.description = description;
        this.reward = reward;
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

    public List<QuestStep> getSteps() {
        return steps;
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
        return currentStep == steps.size();
    }
}
