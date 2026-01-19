package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.ai.Messages;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogSelector;
import com.github.tilcob.game.event.DialogFinishedEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestManager;

import java.util.Map;

public class DialogQuestBridgeSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final Map<String, DialogData> allDialogs;
    private final QuestManager questManager;

    public DialogQuestBridgeSystem(GameEventBus eventBus, Map<String, DialogData> allDialogs,
                                   QuestManager questManager) {
        this.eventBus = eventBus;
        this.allDialogs = allDialogs;
        this.questManager = questManager;

        eventBus.subscribe(DialogFinishedEvent.class, this::onDialogFinished);
    }

    private void onDialogFinished(DialogFinishedEvent event) {
        Entity npcEntity = event.npc();
        Entity player = event.player();
        if (npcEntity == null || player == null) return;

        setFirstContactFlag(npcEntity, player);
        notifyQuestTalk(npcEntity, player);
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
        QuestLog questLog = QuestLog.MAPPER.get(player);
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;
        questManager.signal(player, "talk", npc.getName(), 1);

        DialogData dialogData = allDialogs.get(npc.getName());
        if (dialogData == null || dialogData.questDialog() == null) return;
        String questId = dialogData.questDialog().questId();

        if (questLog != null) {
            Quest quest = questLog.getQuestById(questId);
            if (quest != null && quest.isCompleted() && !quest.isRewardClaimed()) {
                eventBus.fire(new QuestRewardEvent(player, questId));
            }
        }
    }

    private void notifyNpcDialogFinished(Entity npcEntity) {
        NpcFsm npcFsm = NpcFsm.MAPPER.get(npcEntity);
        if (npcFsm == null) return;
        Telegram telegram = new Telegram();
        telegram.message = Messages.DIALOG_FINISHED;
        npcFsm.getNpcFsm().handleMessage(telegram);
    }


    @Override
    public void dispose() {
        eventBus.unsubscribe(DialogFinishedEvent.class, this::onDialogFinished);
    }
}
