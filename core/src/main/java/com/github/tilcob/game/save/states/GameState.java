package com.github.tilcob.game.save.states;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.tilcob.game.assets.MapAsset;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameState {
    private MapAsset currentMap;
    private PlayerState playerState;
    private ChestRegistryState chestRegistryState;
    private int saveVersion = 1;

    public GameState() {}

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public ChestRegistryState getChestRegistryState() {
        return chestRegistryState;
    }

    public void setChestRegistryState(ChestRegistryState chestRegistryState) {
        this.chestRegistryState = chestRegistryState;
    }

    public MapAsset getCurrentMap() {
        return currentMap;
    }
    public void setCurrentMap(MapAsset currentMap) {
        this.currentMap = currentMap;
    }

    public int getSaveVersion() {
        return saveVersion;
    }

    public void setSaveVersion(int saveVersion) {
        this.saveVersion = saveVersion;
    }
}
