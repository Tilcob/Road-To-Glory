package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.ai.Messages;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogSelector;
import com.github.tilcob.game.dialog.QuestDialog;
import com.github.tilcob.game.event.DialogFinishedEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.quest.QuestDefinition;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.quest.QuestYarnRegistry;

import java.util.Map;

public class DialogQuestBridgeSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestManager questManager;
    private final QuestYarnRegistry questYarnRegistry;
    private final Map<String, DialogData> allDialogs;

    public DialogQuestBridgeSystem(GameEventBus eventBus, QuestManager questManager,
                                   QuestYarnRegistry questYarnRegistry, Map<String, DialogData> allDialogs) {
        this.eventBus = eventBus;
        this.questManager = questManager;
        this.questYarnRegistry = questYarnRegistry;
        this.allDialogs = allDialogs;

        eventBus.subscribe(DialogFinishedEvent.class, this::onDialogFinished);
    }

    private void onDialogFinished(DialogFinishedEvent event) {
        Entity npcEntity = event.npc();
        Entity player = event.player();
        if (npcEntity == null || player == null) return;

        setFirstContactFlag(npcEntity, player);
        notifyQuestTalk(npcEntity, player);
        maybeNotifyQuestReward(player, npcEntity);
        notifyNpcDialogFinished(npcEntity);
    }

    private void setFirstContactFlag(Entity npcEntity, Entity player) {
        DialogFlags dialogFlags = DialogFlags.MAPPER.get(player);
        if (dialogFlags == null) return;

        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;

        String flagKey = DialogSelector.firstContactFlagKey(npc.getName());
        if (flagKey == null) return;
        dialogFlags.set(flagKey, true);
    }

    private void notifyQuestTalk(Entity npcEntity, Entity player) {
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;
        questManager.signal(player, "talk", npc.getName(), 1);
    }

    private void notifyNpcDialogFinished(Entity npcEntity) {
        NpcFsm npcFsm = NpcFsm.MAPPER.get(npcEntity);
        if (npcFsm == null) return;
        Telegram telegram = new Telegram();
        telegram.message = Messages.DIALOG_FINISHED;
        npcFsm.getNpcFsm().handleMessage(telegram);
    }

    private void maybeNotifyQuestReward(Entity player, Entity npcEntity) {
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;
        DialogData dialogData = allDialogs.get(npc.getName());
        if (dialogData == null) return;
        QuestDialog questDialog = dialogData.questDialog();
        if (questDialog == null || questDialog.questId() == null) return;
        QuestDefinition definition = questDefinitionFor(questDialog.questId());
        if (definition == null || definition.rewardTiming() != QuestDefinition.RewardTiming.GIVER) return;
        eventBus.fire(new QuestRewardEvent(player, questDialog.questId()));
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
    }
}
