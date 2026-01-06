package com.github.tilcob.game.save;

import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.PlayerState;

public class StateManager {
    private final GameState gameState;

    public StateManager(GameState gameState) {
        this.gameState = gameState;
    }

    public void saveChestRegistry(ChestRegistry chestRegistry) {
        gameState.setChestRegistry(chestRegistry);
    }

    public void savePlayerState(PlayerState playerState) {
        gameState.setPlayerState(playerState);
    }

    public void saveMap(MapAsset map) {
        gameState.setCurrentMap(map);
    }

    public GameState getGameState() {
        return gameState;
    }
}
