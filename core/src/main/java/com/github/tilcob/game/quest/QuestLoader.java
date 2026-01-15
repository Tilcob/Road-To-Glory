package com.github.tilcob.game.quest;

import com.github.tilcob.game.quest.step.QuestStep;
import com.github.tilcob.game.save.states.quest.QuestState;

import java.util.List;

public class QuestLoader {
    private final QuestFactory factory;

    public QuestLoader(QuestFactory factory) {
        this.factory = factory;
    }

    public Quest loadQuest(QuestState state) {
        Quest quest = factory.create(state.getQuestId());
        quest.setCurrentStep(state.getCurrentStep());
        quest.setRewardClaimed(state.isRewardClaimed());

        List<Object> data = state.getStepData();
        List<QuestStep> steps = quest.getSteps();

        for (int i = 0; i < data.size(); i++) {
            steps.get(i).loadData(data.get(i));
        }
        return quest;
    }

}
