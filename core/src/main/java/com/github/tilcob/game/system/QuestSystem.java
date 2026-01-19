package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestDefinition;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

public class QuestSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestFactory factory;
    private final QuestYarnRegistry questYarnRegistry;
    private final QuestYarnRuntime questYarnRuntime;

    public QuestSystem(GameEventBus eventBus, QuestYarnRegistry questYarnRegistry, QuestYarnRuntime questYarnRuntime) {
        super(Family.all(QuestLog.class).get());
        this.eventBus = eventBus;
        this.questYarnRegistry = questYarnRegistry;
        this.factory = new QuestFactory(questYarnRegistry);
        this.questYarnRuntime = questYarnRuntime;

        eventBus.subscribe(AddQuestEvent.class, this::addQuest);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        QuestLog questLog = QuestLog.MAPPER.get(entity);
        boolean updated = false;

        var quests = questLog.getQuests();

        for (int i = 0; i < quests.size; i++) {
            Quest quest = quests.get(i);
            if (!quest.isCompleted() || quest.isCompletionNotified()) continue;
            quest.setCompletionNotified(true);
            updated = true;
            eventBus.fire(new QuestCompletedEvent(entity, quest.getQuestId()));
        }

        if (updated) eventBus.fire(new UpdateQuestLogEvent(entity));
    }

    private void addQuest(AddQuestEvent event) {
        QuestLog questLog = QuestLog.MAPPER.get(event.player());
        for (Quest quest : questLog.getQuests()) {
            if (quest.getQuestId().equals(event.questId())) return;
        }
        Quest quest = factory.create(event.questId());
        questLog.add(quest);
        if (quest.getTotalStages() == 0) {
            quest.setCompletionNotified(true);
            eventBus.fire(new QuestCompletedEvent(event.player(), event.questId()));
        }
        QuestDefinition definition = questYarnRegistry.getQuestDefinition(event.questId());
        if (definition != null) {
            questYarnRuntime.executeStartNode(event.player(), definition.startNode());
        }
        eventBus.fire(new UpdateQuestLogEvent(event.player()));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(AddQuestEvent.class, this::addQuest);
    }
}
