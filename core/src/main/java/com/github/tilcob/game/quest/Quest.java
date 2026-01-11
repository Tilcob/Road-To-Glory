package com.github.tilcob.game.quest;

import com.github.tilcob.game.quest.step.QuestStep;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String questId;
    private final List<QuestStep> steps = new ArrayList<>();
    private int currentStep = 0;

    public Quest(String questId) {
        this.questId = questId;
    }

    public String getQuestId() {
        return questId;
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
