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
import com.github.tilcob.game.dialog.*;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.quest.QuestState;

import java.util.Map;

public class DialogSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final Map<String, MapDialogData> allDialogs;
    private String mapId;

    public DialogSystem(GameEventBus eventBus, Map<String, MapDialogData> allDialogs) {
        super(Family.all(Dialog.class).get());
        this.eventBus = eventBus;
        this.allDialogs = allDialogs;

        eventBus.subscribe(DialogAdvanceEvent.class, this::onDialogAdvance);
        eventBus.subscribe(DialogChoiceNavigateEvent.class, this::onChoiceNavigate);
        eventBus.subscribe(DialogChoiceSelectEvent.class, this::onChoiceSelect);
        eventBus.subscribe(ExitTriggerEvent.class, this::onExitTrigger);
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

        notifyQuestTalk(npcEntity);
    }

    private void notifyQuestTalk(Entity npcEntity) {
        PlayerReference playerReference = PlayerReference.MAPPER.get(npcEntity);
        if (playerReference == null) return;
        Entity player = playerReference.getPlayer();
        if (player == null) return;

        QuestLog questLog = QuestLog.MAPPER.get(player);
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;
        eventBus.fire(new QuestStepEvent(QuestStepEvent.Type.TALK, npc.getName()));

        if (mapId == null) return;
        MapDialogData mapDialogData = allDialogs.get(mapId);
        if (mapDialogData == null) return;

        DialogData dialogData = mapDialogData.getNpcs().get(npc.getName());
        if (dialogData == null || dialogData.questDialog() == null) return;
        String questId = dialogData.questDialog().questId();
        QuestState questState = questLog.getQuestStateById(questId);
        if (questState == QuestState.NOT_STARTED) {
            eventBus.fire(new AddQuestEvent(player, questId));
        }

        Telegram telegram = new Telegram();
        telegram.message = Messages.DIALOG_FINISHED;
        NpcFsm.MAPPER.get(npcEntity).getNpcFsm().handleMessage(telegram);
    }

    private void startDialog(Entity npcEntity, Dialog dialog) {
        Npc npc = Npc.MAPPER.get(npcEntity);
        PlayerReference playerReference = PlayerReference.MAPPER.get(npcEntity);
        Entity player = playerReference == null ? null : playerReference.getPlayer();
        if (player == null || mapId == null) {
            dialog.setState(Dialog.State.IDLE);
            return;
        }
        if (DialogSession.MAPPER.get(player) != null) return;
        MapDialogData mapDialogData = allDialogs.get(mapId);
        if (mapDialogData == null) {
            dialog.setState(Dialog.State.IDLE);
            return;
        }
        DialogData dialogData = mapDialogData.getNpcs().get(npc.getName());
        if (dialogData == null) {
            dialog.setState(Dialog.State.IDLE);
            return;
        }
        QuestLog questLog = QuestLog.MAPPER.get(player);
        DialogFlags dialogFlags = DialogFlags.MAPPER.get(player);

        Array<String> lines = DialogSelector.select(dialogData, questLog, dialogFlags);
        boolean repeatChoices = dialogData.questDialog() == null;
        DialogSession session = new DialogSession(npcEntity, lines, dialogData.choices(), repeatChoices);
        if (!session.hasLines()) {
            dialog.setState(Dialog.State.IDLE);
            return;
        }
        dialog.setState(Dialog.State.ACTIVE);
        NpcFsm.MAPPER.get(npcEntity).getNpcFsm().changeState(NpcState.TALKING);
        player.add(session);
        eventBus.fire(new DialogEvent(toDialogLine(session), npcEntity));
    }

    private DialogLine toDialogLine(DialogSession session) {
        return new DialogLine(session.currentLine(), session.getIndex() + 1, session.getTotal());
    }

    private void onMapChange(MapChangeEvent event) {
        this.mapId = event.mapId();
    }

    private void onFinishedDialog(FinishedDialogEvent event) {
        Entity npc = event.entity();
        PlayerReference playerReference = PlayerReference.MAPPER.get(npc);
        if (playerReference != null) {
            Entity player = playerReference.getPlayer();
            if (player != null && DialogSession.MAPPER.get(player) != null) {
                player.remove(DialogSession.class);
            }
        }
        Dialog dialog = Dialog.MAPPER.get(npc);
        dialog.setState(Dialog.State.FINISHED);
    }

    private void onDialogAdvance(DialogAdvanceEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        if (session == null) {
            return;
        }
        if (session.advance()) {
            eventBus.fire(new DialogEvent(toDialogLine(session), session.getNpc()));
        } else {
            if (session.hasRemainingChoices() && !session.isAwaitingChoice()) {
                session.beginChoice();
                eventBus.fire(new DialogChoiceEvent(session.getChoices(), session.getChoiceIndex(), session.getNpc()));
            } else {
                player.remove(DialogSession.class);
                eventBus.fire(new FinishedDialogEvent(Messages.DIALOG_FINISHED, session.getNpc()));
            }
        }
    }

    private void onChoiceNavigate(DialogChoiceNavigateEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        if (session == null || !session.isAwaitingChoice()) {
            return;
        }
        session.moveChoice(event.delta());
        eventBus.fire(new DialogChoiceEvent(session.getChoices(), session.getChoiceIndex(), session.getNpc()));
    }

    private void onChoiceSelect(DialogChoiceSelectEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        if (session == null || !session.isAwaitingChoice()) return;

        DialogChoice choice = session.selectChoice();
        eventBus.fire(new DialogChoiceEvent(new Array<>(), 0, session.getNpc()));
        if (choice == null) return;

        eventBus.fire(new DialogChoiceResolvedEvent(player, session.getNpc(), choice));
        session.setLines(choice.lines());
        if (!session.hasLines()) {
            player.remove(DialogSession.class);
            eventBus.fire(new FinishedDialogEvent(Messages.DIALOG_FINISHED, session.getNpc()));
            return;
        }
        eventBus.fire(new DialogEvent(toDialogLine(session), session.getNpc()));
    }

    private void onExitTrigger(ExitTriggerEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        if (session == null) {
            return;
        }
        Entity npcEntity = session.getNpc();
        if (npcEntity != null && npcEntity.equals(event.trigger())) {
            player.remove(DialogSession.class);
            Dialog dialog = Dialog.MAPPER.get(npcEntity);
            if (dialog != null) {
                dialog.setState(Dialog.State.IDLE);
            }
            NpcFsm npcFsm = NpcFsm.MAPPER.get(npcEntity);
            if (npcFsm != null) {
                npcFsm.getNpcFsm().changeState(NpcState.IDLE);
            }
        }
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(DialogAdvanceEvent.class, this::onDialogAdvance);
        eventBus.unsubscribe(DialogChoiceNavigateEvent.class, this::onChoiceNavigate);
        eventBus.unsubscribe(DialogChoiceSelectEvent.class, this::onChoiceSelect);
        eventBus.unsubscribe(ExitTriggerEvent.class, this::onExitTrigger);
        eventBus.unsubscribe(MapChangeEvent.class, this::onMapChange);
        eventBus.unsubscribe(FinishedDialogEvent.class, this::onFinishedDialog);
    }
}
