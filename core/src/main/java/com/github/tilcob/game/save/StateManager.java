package com.github.tilcob.game.save;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.player.PlayerStateExtractor;
import com.github.tilcob.game.save.states.ChestRegistryState;
import com.github.tilcob.game.save.states.GameState;

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
}
