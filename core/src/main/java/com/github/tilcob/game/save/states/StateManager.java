package com.github.tilcob.game.save.states;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.player.PlayerStateExtractor;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestLoader;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;
import com.github.tilcob.game.save.states.quest.QuestState;

public class StateManager {
    private GameState gameState;

    public StateManager(GameState gameState) {
        this.gameState = gameState;
    }

    public void saveChestRegistryState(ChestRegistryState chestRegistryState) {
        gameState.setChestRegistryState(chestRegistryState);
    }

    public ChestRegistryState loadChestRegistryState() {
        return gameState.getChestRegistryState();
    }

    public void setPlayerState(Entity player) {
        gameState.setPlayerState(PlayerStateExtractor.fromEntity(player));
    }

    public void saveMap(MapAsset map) {
        gameState.setCurrentMap(map);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void saveQuests(QuestLog questLog) {
        gameState.getQuests().clear();
        for (Quest questState : questLog.getQuests()) {
            gameState.getQuests().add(new QuestState(questState));
        }
    }

    public void loadQuests(QuestLog questLog, QuestLoader loader) {
        questLog.getQuests().clear();
        for (QuestState state : gameState.getQuests()) {
            Quest quest = loader.loadQuest(state);
            questLog.add(quest);
        }
    }

    public void saveDialogFlags(DialogFlags dialogFlags) {
        gameState.getDialogFlags().clear();
        if (dialogFlags == null) {
            return;
        }
        for (var entry : dialogFlags.getFlags()) {
            gameState.getDialogFlags().put(entry.key, entry.value);
        }
    }

    public void loadDialogFlags(DialogFlags dialogFlags) {
        if (dialogFlags == null) {
            return;
        }
        dialogFlags.getFlags().clear();
        for (var entry : gameState.getDialogFlags().entrySet()) {
            dialogFlags.set(entry.getKey(), entry.getValue() != null && entry.getValue());
        }
    }
}
