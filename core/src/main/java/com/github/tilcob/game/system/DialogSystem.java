package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.ai.Messages;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogSelector;
import com.github.tilcob.game.dialog.MapDialogData;
import com.github.tilcob.game.event.DialogEvent;
import com.github.tilcob.game.event.FinishedDialogEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.MapChangeEvent;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.step.QuestStep;

import java.util.Map;

public class DialogSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final Map<String, MapDialogData> allDialogs;
    private String mapId;

    public DialogSystem(GameEventBus eventBus, Map<String, MapDialogData> allDialogs) {
        super(Family.all(Dialog.class).get());
        this.eventBus = eventBus;
        this.allDialogs = allDialogs;

        eventBus.subscribe(MapChangeEvent.class, this::onMapChange);
        eventBus.subscribe(FinishedDialogEvent.class, this::onFinishedDialog);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Dialog dialog = Dialog.MAPPER.get(entity);

        switch (dialog.getState()) {
            case REQUEST -> startDialog(entity, dialog);
            case ACTIVE -> {

            }
            case FINISHED -> finishDialog(entity, dialog);
        }
    }

    private void finishDialog(Entity npcEntity, Dialog dialog) {
        dialog.setState(Dialog.State.IDLE);

        updateQuestProgress(npcEntity);
    }

    private void updateQuestProgress(Entity npcEntity) {
        Entity player = PlayerReference.MAPPER.get(npcEntity).getPlayer();
        QuestLog questLog = QuestLog.MAPPER.get(player);
        Npc npc = Npc.MAPPER.get(npcEntity);

        MapDialogData mapDialogData = allDialogs.get(mapId);
        DialogData dialogData = mapDialogData.getNpcs().get(npc.getName());
        if (dialogData.questDialog() != null) {
            Quest quest = questLog.getQuestById(dialogData.questDialog().questId());
            if (quest != null && !quest.isCompleted()) {
                QuestStep step = quest.getSteps().get(quest.getCurrentStep());
                quest.incCurrentStep();
            }
        }

        Telegram telegram = new Telegram();
        telegram.message = Messages.DIALOG_FINISHED;
        Fsm.MAPPER.get(npcEntity).getNpcFsm().handleMessage(telegram);
    }

    private void startDialog(Entity npcEntity, Dialog dialog) {
        dialog.setState(Dialog.State.ACTIVE);
        Fsm.MAPPER.get(npcEntity).getNpcFsm().changeState(NpcState.TALKING);

        Npc npc = Npc.MAPPER.get(npcEntity);
        Entity player = PlayerReference.MAPPER.get(npcEntity).getPlayer();
        MapDialogData mapDialogData = allDialogs.get(mapId);
        DialogData dialogData = mapDialogData.getNpcs().get(npc.getName());
        QuestLog questLog = QuestLog.MAPPER.get(player);

        Array<String> lines = DialogSelector.select(dialogData.idle(), dialogData.questDialog(), questLog);
        eventBus.fire(new DialogEvent(lines, npcEntity));
    }

    private void onMapChange(MapChangeEvent event) {
        this.mapId = event.mapId();
    }

    private void onFinishedDialog(FinishedDialogEvent event) {
        Entity npc = event.entity();
        Dialog dialog = Dialog.MAPPER.get(npc);
        dialog.setState(Dialog.State.FINISHED);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(MapChangeEvent.class, this::onMapChange);
        eventBus.unsubscribe(FinishedDialogEvent.class, this::onFinishedDialog);
    }
}
