package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.QuestDialog;
import com.github.tilcob.game.event.DialogFinishedEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.quest.QuestDefinition;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.quest.RewardTiming;

import java.util.Map;

public class QuestRewardSchedulerSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestYarnRegistry questYarnRegistry;
    private final Map<String, DialogData> allDialogs;

    public QuestRewardSchedulerSystem(GameEventBus eventBus, QuestYarnRegistry questYarnRegistry,
                                      Map<String, DialogData> allDialogs) {
        this.eventBus = eventBus;
        this.questYarnRegistry = questYarnRegistry;
        this.allDialogs = allDialogs;

        eventBus.subscribe(DialogFinishedEvent.class, this::onDialogFinished);
        eventBus.subscribe(QuestCompletedEvent.class, this::onQuestCompleted);
    }

    private void onDialogFinished(DialogFinishedEvent event) {
        Entity npcEntity = event.npc();
        Entity player = event.player();
        if (npcEntity == null || player == null) return;
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;
        DialogData dialogData = allDialogs.get(npc.getName());
        if (dialogData == null) return;
        QuestDialog questDialog = dialogData.questDialog();
        if (questDialog == null || questDialog.questId() == null) return;
        QuestDefinition definition = questDefinitionFor(questDialog.questId());
        if (definition == null || definition.rewardTiming() != RewardTiming.GIVER) return;
        eventBus.fire(new QuestRewardEvent(player, questDialog.questId()));
    }

    private void onQuestCompleted(QuestCompletedEvent event) {
        QuestDefinition definition = questDefinitionFor(event.questId());
        if (definition == null) return;
        RewardTiming timing = definition.rewardTiming();
        if (timing != RewardTiming.COMPLETION && timing != RewardTiming.AUTO) return;
        eventBus.fire(new QuestRewardEvent(event.player(), event.questId()));
    }

    private QuestDefinition questDefinitionFor(String questId) {
        if (questYarnRegistry.isEmpty()) {
            questYarnRegistry.loadAll();
        }
        return questYarnRegistry.getQuestDefinition(questId);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(DialogFinishedEvent.class, this::onDialogFinished);
        eventBus.unsubscribe(QuestCompletedEvent.class, this::onQuestCompleted);
    }
}
