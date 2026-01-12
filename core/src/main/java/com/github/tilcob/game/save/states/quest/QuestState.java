package com.github.tilcob.game.save.states.quest;

import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.step.QuestStep;

import java.util.ArrayList;
import java.util.List;

public class QuestState {
    private String questId;
    private int currentStep;
    private List<Object> stepData = new ArrayList<>();

    public QuestState() {}

    public QuestState(Quest quest) {
        this.questId = quest.getQuestId();
        this.currentStep = quest.getCurrentStep();
        for (QuestStep questStep : quest.getSteps()) {
            stepData.add(questStep.saveData());
        }
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public List<Object> getStepData() {
        return stepData;
    }

    public void setStepData(List<Object> stepData) {
        this.stepData = stepData;
    }
}
