package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.event.AddQuestEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestLifecycleService;

public class QuestSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestLifecycleService questLifecycleService;

    public QuestSystem(GameEventBus eventBus, QuestLifecycleService questLifecycleService) {
        super(Family.all(QuestLog.class).get());
        this.eventBus = eventBus;
        this.questLifecycleService = questLifecycleService;

        eventBus.subscribe(AddQuestEvent.class, this::addQuest);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        QuestLog questLog = QuestLog.MAPPER.get(entity);
        var quests = questLog.getQuests();

        for (int i = 0; i < quests.size; i++) {
            Quest quest = quests.get(i);
            questLifecycleService.notifyQuestCompletion(entity, quest);
        }
    }

    private void addQuest(AddQuestEvent event) {
        questLifecycleService.startQuest(event.player(), event.questId());
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(AddQuestEvent.class, this::addQuest);
    }
}
