package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.event.DialogFinishedEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.quest.QuestLifecycleService;

public class QuestRewardSchedulerSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestLifecycleService questLifecycleService;

    public QuestRewardSchedulerSystem(GameEventBus eventBus, QuestLifecycleService questLifecycleService) {
        this.eventBus = eventBus;
        this.questLifecycleService = questLifecycleService;

        eventBus.subscribe(DialogFinishedEvent.class, this::onDialogFinished);
        eventBus.subscribe(QuestCompletedEvent.class, this::onQuestCompleted);
    }

    private void onDialogFinished(DialogFinishedEvent event) {
        questLifecycleService.scheduleRewardFromDialog(event.npc(), event.player());
    }

    private void onQuestCompleted(QuestCompletedEvent event) {
        questLifecycleService.scheduleRewardFromCompletion(event.player(), event.questId());
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(DialogFinishedEvent.class, this::onDialogFinished);
        eventBus.unsubscribe(QuestCompletedEvent.class, this::onQuestCompleted);
    }
}
