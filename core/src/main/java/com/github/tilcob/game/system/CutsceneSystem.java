package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Cutscene;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.cutscene.CutsceneData;
import com.github.tilcob.game.event.CutsceneFinishedEvent;
import com.github.tilcob.game.event.CutsceneRequestedEvent;
import com.github.tilcob.game.event.DialogFinishedEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.yarn.CutsceneCommandResult;
import com.github.tilcob.game.yarn.CutsceneYarnRuntime;

import java.util.Map;

public class CutsceneSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final Map<String, CutsceneData> allCutscenes;
    private final CutsceneYarnRuntime cutsceneYarnRuntime;

    public CutsceneSystem(GameEventBus eventBus,
                          Map<String, CutsceneData> allCutscenes,
                          CutsceneYarnRuntime cutsceneYarnRuntime) {
        super(Family.all(Cutscene.class).get());
        this.eventBus = eventBus;
        this.allCutscenes = allCutscenes;
        this.cutsceneYarnRuntime = cutsceneYarnRuntime;

        eventBus.subscribe(CutsceneRequestedEvent.class, this::onCutsceneRequested);
        eventBus.subscribe(QuestCompletedEvent.class, this::onQuestCompleted);
        eventBus.subscribe(DialogFinishedEvent.class, this::onDialogFinished);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Cutscene cutscene = Cutscene.MAPPER.get(entity);
        switch (cutscene.getState()) {
            case REQUEST -> startCutscene(entity, cutscene);
            case ACTIVE -> updateCutscene(entity, cutscene, deltaTime);
            case FINISHED -> finishCutscene(entity, cutscene);
            case IDLE -> {
            }
        }
    }

    private void startCutscene(Entity player, Cutscene cutscene) {
        CutsceneData data = getCutsceneData(cutscene.getCutsceneId());
        if (data == null) {
            cutscene.setState(Cutscene.State.IDLE);
            return;
        }
        cutscene.setLineIndex(0);
        cutscene.setWaitTimerSeconds(0f);
        cutscene.setAwaitingDialog(false);
        cutscene.setState(Cutscene.State.ACTIVE);
    }

    private void updateCutscene(Entity player, Cutscene cutscene, float deltaTime) {
        if (cutscene.isAwaitingDialog()) {
            return;
        }
        if (cutscene.getWaitTimerSeconds() > 0f) {
            cutscene.setWaitTimerSeconds(cutscene.getWaitTimerSeconds() - deltaTime);
            return;
        }
        CutsceneData data = getCutsceneData(cutscene.getCutsceneId());
        if (data == null) {
            cutscene.setState(Cutscene.State.FINISHED);
            return;
        }
        Array<String> lines = data.lines();
        if (lines == null || lines.isEmpty()) {
            cutscene.setState(Cutscene.State.FINISHED);
            return;
        }
        while (cutscene.getLineIndex() < lines.size) {
            String line = lines.get(cutscene.getLineIndex());
            CutsceneCommandResult result = cutsceneYarnRuntime.executeCommandLine(player, line);
            cutscene.setLineIndex(cutscene.getLineIndex() + 1);
            if (result.waitForDialog()) {
                cutscene.setAwaitingDialog(true);
                return;
            }
            if (result.waitSeconds() > 0f) {
                cutscene.setWaitTimerSeconds(result.waitSeconds());
                return;
            }
        }
        cutscene.setState(Cutscene.State.FINISHED);
    }

    private void finishCutscene(Entity player, Cutscene cutscene) {
        cutscene.setState(Cutscene.State.IDLE);
        eventBus.fire(new CutsceneFinishedEvent(player, cutscene.getCutsceneId()));
    }

    private void onCutsceneRequested(CutsceneRequestedEvent event) {
        requestCutscene(event.player(), event.cutsceneId());
    }

    private void onQuestCompleted(QuestCompletedEvent event) {
        requestCutscene(event.player(), event.questId());
    }

    private void onDialogFinished(DialogFinishedEvent event) {
        Entity player = event.player();
        Cutscene cutscene = player == null ? null : Cutscene.MAPPER.get(player);
        if (cutscene != null && cutscene.isAwaitingDialog()) {
            cutscene.setAwaitingDialog(false);
            return;
        }
        Entity npc = event.npc();
        if (npc == null) return;
        Npc npcComponent = Npc.MAPPER.get(npc);
        if (npcComponent == null) return;
        requestCutscene(player, npcComponent.getName());
    }

    private void requestCutscene(Entity player, String cutsceneId) {
        if (player == null || cutsceneId == null || cutsceneId.isBlank()) {
            return;
        }
        if (!allCutscenes.containsKey(cutsceneId)) {
            return;
        }
        Cutscene cutscene = Cutscene.MAPPER.get(player);
        if (cutscene == null) {
            player.add(new Cutscene(cutsceneId));
            return;
        }
        if (cutscene.getState() == Cutscene.State.ACTIVE || cutscene.getState() == Cutscene.State.REQUEST) {
            return;
        }
        cutscene.setCutsceneId(cutsceneId);
        cutscene.setLineIndex(0);
        cutscene.setWaitTimerSeconds(0f);
        cutscene.setAwaitingDialog(false);
        cutscene.setState(Cutscene.State.REQUEST);
    }

    private CutsceneData getCutsceneData(String cutsceneId) {
        if (cutsceneId == null || cutsceneId.isBlank()) return null;
        return allCutscenes.get(cutsceneId);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CutsceneRequestedEvent.class, this::onCutsceneRequested);
        eventBus.unsubscribe(QuestCompletedEvent.class, this::onQuestCompleted);
        eventBus.unsubscribe(DialogFinishedEvent.class, this::onDialogFinished);
    }
}
