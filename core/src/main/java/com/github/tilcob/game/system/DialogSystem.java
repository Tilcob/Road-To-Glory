package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.ai.Messages;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.dialog.*;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.flow.CommandCall;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.IfStack;
import com.github.tilcob.game.yarn.script.ScriptEvent;

import java.util.Map;

public class DialogSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final Map<String, DialogData> allDialogs;
    private final ObjectMap<Entity, DialogNavigator> navigators;
    private final DialogYarnRuntime dialogYarnRuntime;
    private final ObjectMap<Entity, IfStack> ifStacks = new ObjectMap<>();

    public DialogSystem(GameEventBus eventBus, Map<String, DialogData> allDialogs, DialogYarnRuntime dialogYarnRuntime) {
        super(Family.all(Dialog.class).get());
        this.eventBus = eventBus;
        this.allDialogs = allDialogs;
        this.navigators = new ObjectMap<>();
        this.dialogYarnRuntime = dialogYarnRuntime;

        eventBus.subscribe(DialogAdvanceEvent.class, this::onDialogAdvance);
        eventBus.subscribe(DialogChoiceNavigateEvent.class, this::onChoiceNavigate);
        eventBus.subscribe(DialogChoiceSelectEvent.class, this::onChoiceSelect);
        eventBus.subscribe(ExitTriggerEvent.class, this::onExitTrigger);
        eventBus.subscribe(FinishedDialogEvent.class, this::onFinishedDialog);
        eventBus.subscribe(StartDialogEvent.class, this::onStartDialog);
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
        PlayerReference playerReference = PlayerReference.MAPPER.get(npcEntity);
        Entity player = playerReference == null ? null : playerReference.getPlayer();
        if (player != null) eventBus.fire(new DialogFinishedEvent(npcEntity, player));
    }

    private void startDialog(Entity npcEntity, Dialog dialog) {
        Npc npc = Npc.MAPPER.get(npcEntity);
        PlayerReference playerReference = PlayerReference.MAPPER.get(npcEntity);
        Entity player = playerReference == null ? null : playerReference.getPlayer();
        if (player == null) {
            dialog.setState(Dialog.State.IDLE);
            return;
        }
        if (DialogSession.MAPPER.get(player) != null) return;

        DialogData dialogData = allDialogs.get(npc.getName());
        if (dialogData == null) {
            dialog.setState(Dialog.State.IDLE);
            return;
        }
        QuestLog questLog = QuestLog.MAPPER.get(player);
        DialogFlags dialogFlags = DialogFlags.MAPPER.get(player);

        DialogSelection selection = DialogSelector.select(dialogData, questLog, dialogFlags);
        boolean repeatChoices = dialogData.questDialog() == null;
        DialogSession session = new DialogSession(npcEntity);
        DialogNavigator navigator = new DialogNavigator(dialogData, session, selection, repeatChoices);
        StartDialogRequest startRequest = StartDialogRequest.MAPPER.get(player);
        String startNodeId = startRequest != null && startRequest.getNpc() == npcEntity ? startRequest.getNodeId() : null;
        if (startNodeId != null && !startNodeId.isBlank()) {
            navigator.startAtNode(startNodeId);
        }
        if (!navigator.hasLines()) {
            dialog.setState(Dialog.State.IDLE);
            return;
        }
        dialog.setState(Dialog.State.ACTIVE);
        NpcFsm.MAPPER.get(npcEntity).getNpcFsm().changeState(NpcState.TALKING);
        player.add(session);
        navigators.put(player, navigator);
        ifStacks.put(player, new IfStack());
        eventBus.fire(new DialogStartedEvent(npcEntity, player));
        if (!dispatchNextLine(player, navigator, npcEntity)) {
            removeSession(player);
            dialog.setState(Dialog.State.IDLE);
            NpcFsm npcFsm = NpcFsm.MAPPER.get(npcEntity);
            if (npcFsm != null) {
                npcFsm.getNpcFsm().changeState(NpcState.IDLE);
            }
        }
    }

    private void onFinishedDialog(FinishedDialogEvent event) {
        Entity npc = event.entity();
        PlayerReference playerReference = PlayerReference.MAPPER.get(npc);
        if (playerReference != null) {
            Entity player = playerReference.getPlayer();
            if (player != null && DialogSession.MAPPER.get(player) != null) {
                removeSession(player);
            }
        }
        Dialog dialog = Dialog.MAPPER.get(npc);
        dialog.setState(Dialog.State.FINISHED);
    }

    private void onDialogAdvance(DialogAdvanceEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        DialogNavigator navigator = navigators.get(player);

        if (session == null || navigator == null) return;
        if (navigator.advance() && dispatchNextLine(player, navigator, session.getNpc())) return;
        if (navigator.hasRemainingChoices() && !session.isAwaitingChoice()) {
            navigator.beginChoice();
            eventBus.fire(new DialogChoiceEvent(navigator.getChoices(), session.getChoiceIndex(), session.getNpc()));
        }  else {
            removeSession(player);
            eventBus.fire(new FinishedDialogEvent(Messages.DIALOG_FINISHED, session.getNpc()));
        }
    }

    private void onChoiceNavigate(DialogChoiceNavigateEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        DialogNavigator navigator = navigators.get(player);
        if (session == null || navigator == null || !session.isAwaitingChoice()) return;
        navigator.moveChoice(event.delta());
        eventBus.fire(new DialogChoiceEvent(navigator.getChoices(), session.getChoiceIndex(), session.getNpc()));
    }

    private void onChoiceSelect(DialogChoiceSelectEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        DialogNavigator navigator = navigators.get(player);
        if (session == null || navigator == null || !session.isAwaitingChoice()) return;

        DialogChoice choice = navigator.selectChoice();
        eventBus.fire(new DialogChoiceEvent(new Array<>(), 0, session.getNpc()));
        if (choice == null) return;

        eventBus.fire(new DialogChoiceResolvedEvent(player, session.getNpc(), choice));
        navigator.applyChoice(choice);

        if (!navigator.hasLines()) {
            removeSession(player);
            eventBus.fire(new FinishedDialogEvent(Messages.DIALOG_FINISHED, session.getNpc()));
            return;
        }
        if (!dispatchNextLine(player, navigator, session.getNpc())) {
            removeSession(player);
            eventBus.fire(new FinishedDialogEvent(Messages.DIALOG_FINISHED, session.getNpc()));
        }
    }

    private void onExitTrigger(ExitTriggerEvent event) {
        Entity player = event.player();
        DialogSession session = DialogSession.MAPPER.get(player);
        if (session == null) {
            return;
        }
        Entity npcEntity = session.getNpc();
        if (npcEntity != null && npcEntity.equals(event.trigger())) {
            removeSession(player);
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

    private void onStartDialog(StartDialogEvent event) {
        Entity player = event.player();
        String npcId = event.npcId();
        if (player == null || npcId == null || npcId.isBlank()) {
            return;
        }
        Entity npcEntity = findNpcById(npcId);
        if (npcEntity == null) {
            return;
        }
        PlayerReference reference = PlayerReference.MAPPER.get(npcEntity);
        if (reference == null) {
            npcEntity.add(new PlayerReference(player));
        } else {
            reference.setPlayer(player);
        }
        player.remove(StartDialogRequest.class);
        player.add(new StartDialogRequest(npcEntity, event.nodeId()));
        Dialog dialog = Dialog.MAPPER.get(npcEntity);
        if (dialog != null) {
            dialog.setState(Dialog.State.REQUEST);
        }
    }

    private Entity findNpcById(String npcId) {
        if (getEngine() == null) return null;
        for (Entity entity : getEngine().getEntitiesFor(Family.all(Npc.class, Dialog.class).get())) {
            Npc npc = Npc.MAPPER.get(entity);
            if (npc != null && npcId.equals(npc.getName())) {
                return entity;
            }
        }
        return null;
    }

    private void removeSession(Entity player) {
        player.remove(DialogSession.class);
        navigators.remove(player);
        ifStacks.remove(player);
    }

    private boolean dispatchNextLine(Entity player, DialogNavigator navigator, Entity npcEntity) {
        DialogSession session = DialogSession.MAPPER.get(player);
        if (session == null) return false;

        IfStack ifStack = ifStacks.get(player);
        if (ifStack == null) {
            ifStack = new IfStack();
            ifStacks.put(player, ifStack);
        }

        while (navigator.hasLines()) {
            ScriptEvent event = navigator.currentLine();
            CommandCall.SourcePos sourcePos = new CommandCall.SourcePos(
                "dialogs",
                session.getCurrentNodeId(),
                session.getLineIndex()
            );

            if (event instanceof ScriptEvent.IfStart ifs) {
                boolean cond = dialogYarnRuntime.evaluateCondition(player, ifs.condition(), sourcePos);
                ifStack.onIfStart(cond);
                if (!navigator.advance()) return false;
                continue;
            }
            if (event instanceof ScriptEvent.Else) {
                ifStack.onElse();
                if (!navigator.advance()) return false;
                continue;
            }
            if (event instanceof ScriptEvent.EndIf) {
                ifStack.onEndIf();
                if (!navigator.advance()) return false;
                continue;
            }

            if (!ifStack.isExecuting()) {
                if (!navigator.advance()) return false;
                continue;
            }

            if (event instanceof ScriptEvent.Command cmd) {
                if (dialogYarnRuntime.tryExecuteCommandLine(player, cmd.raw(), sourcePos)) {
                    if (!navigator.advance()) return false;
                    continue;
                }
                if (!navigator.advance()) return false;
                continue;
            }

            if (event instanceof ScriptEvent.Text text) {
                eventBus.fire(new DialogEvent(navigator.toDialogLine(), npcEntity));
                return true;
            }

            if (!navigator.advance()) return false;
        }
        return false;
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(DialogAdvanceEvent.class, this::onDialogAdvance);
        eventBus.unsubscribe(DialogChoiceNavigateEvent.class, this::onChoiceNavigate);
        eventBus.unsubscribe(DialogChoiceSelectEvent.class, this::onChoiceSelect);
        eventBus.unsubscribe(ExitTriggerEvent.class, this::onExitTrigger);
        eventBus.unsubscribe(FinishedDialogEvent.class, this::onFinishedDialog);
        eventBus.unsubscribe(StartDialogEvent.class, this::onStartDialog);
    }
}
