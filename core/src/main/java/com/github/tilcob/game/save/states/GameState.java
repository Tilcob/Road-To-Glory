package com.github.tilcob.game.save.states;

import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.save.registry.ChestRegistry;

public class GameState {
    private MapAsset currentMap;
    private PlayerState playerState;
    private ChestRegistry chestRegistry;

    public MapAsset getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(MapAsset currentMap) {
        this.currentMap = currentMap;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }
    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public ChestRegistry getChestRegistry() {
        return chestRegistry;
    }

    public void setChestRegistry(ChestRegistry chestRegistry) {
        this.chestRegistry = chestRegistry;
    }
}
