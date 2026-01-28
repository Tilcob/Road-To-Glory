package com.github.tilcob.game.flow.commands;

import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.RequestCompleteQuestEvent;
import com.github.tilcob.game.event.RequestNextStageQuestEvent;
import com.github.tilcob.game.event.RequestStartQuestEvent;
import com.github.tilcob.game.quest.QuestLifecycleService;

public class QuestCommandHandler {
    private final QuestLifecycleService service;

    public QuestCommandHandler(GameEventBus eventBus, QuestLifecycleService service) {
        this.service = service;

        eventBus.subscribe(RequestStartQuestEvent.class, this::startQuest);
        eventBus.subscribe(RequestCompleteQuestEvent.class, this::completeQuest);
        eventBus.subscribe(RequestNextStageQuestEvent.class, this::nextStage);
    }

    private void nextStage(RequestNextStageQuestEvent event) {
        service.setQuestStage(event.player(), event.questId(), event.stage());
    }

    private void completeQuest(RequestCompleteQuestEvent event) {
        service.completeQuest(event.player(), event.questId());
    }

    private void startQuest(RequestStartQuestEvent event) {
        service.startQuest(event.player(), event.questId());
    }
}
