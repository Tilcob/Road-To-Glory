package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.step.QuestStep;

public class QuestSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestFactory factory;

    public QuestSystem(GameEventBus eventBus) {
        super(Family.all(QuestLog.class).get());
        this.eventBus = eventBus;
        this.factory = new QuestFactory(eventBus);

        eventBus.subscribe(AddQuestEvent.class, this::addQuest);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        QuestLog questLog = QuestLog.MAPPER.get(entity);
        boolean updated = false;

        var quests = questLog.getQuests();

        for (int i = 0; i < quests.size; i++) {
            Quest quest = quests.get(i);
            if (quest.isCompleted()) continue;

            int currentStep = quest.getCurrentStep();
            QuestStep step = quest.getSteps().get(currentStep);

            if (step.completed()) {
                quest.incCurrentStep();
                updated = true;

                if (quest.getCurrentStep() == quest.getSteps().size()) {
                    eventBus.fire(new QuestCompletedEvent(entity, quest.getQuestId()));
                } else {
                    quest.getSteps()
                        .get(quest.getCurrentStep())
                        .start();
                }
            }
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
        if (quest.getSteps().isEmpty()) eventBus.fire(new QuestCompletedEvent(event.player(), event.questId()));
        eventBus.fire(new UpdateQuestLogEvent(event.player()));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(AddQuestEvent.class, this::addQuest);
    }
}
