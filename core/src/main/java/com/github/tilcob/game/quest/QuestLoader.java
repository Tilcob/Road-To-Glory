package com.github.tilcob.game.quest;

import com.github.tilcob.game.save.states.quest.QuestState;

public class QuestLoader {
    private final QuestFactory factory;

    public QuestLoader(QuestFactory factory) {
        this.factory = factory;
    }

    public Quest loadQuest(QuestState state) {
        if (state == null || state.getQuestId() == null || state.getQuestId().isBlank()) return null;
        if (!state.isActive() && !state.isCompleted()) return null;
        Quest quest = factory.create(state.getQuestId());

        int maxStage = quest.getTotalStages();
        int stage = Math.max(0, state.getStage());
        if (state.isCompleted()) {
            quest.setCurrentStep(maxStage);
            quest.setCompletionNotified(true);
        } else {
            quest.setCurrentStep(Math.min(stage, maxStage));
        }
        quest.setRewardClaimed(state.isRewardClaimed());
        return quest;
    }

}
