package com.github.tilcob.game.save.states;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;
import com.github.tilcob.game.save.states.chest.ChestState;
import com.github.tilcob.game.save.states.quest.QuestState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameState {
    @JsonIgnore
    private MapAsset currentMap;
    private String currentMapByName;
    private PlayerState playerState;
    private ChestRegistryState chestRegistryState;
    private List<QuestState> quests = new ArrayList<>();
    private Map<String, Boolean> dialogFlags = new HashMap<>();
    private Map<String, Integer> counters = new HashMap<>();
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

    public List<QuestState> getQuests() {
        return quests;
    }

    public void setQuests(List<QuestState> quests) {
        this.quests = quests;
    }

    public Map<String, Boolean> getDialogFlags() {
        return dialogFlags;
    }

    public void setDialogFlags(Map<String, Boolean> dialogFlags) {
        this.dialogFlags = dialogFlags;
    }

    public Map<String, Integer> getCounters() {
        return counters;
    }

    public void setCounters(Map<String, Integer> counters) {
        this.counters = counters;
    }

    @JsonIgnore
    public MapAsset getCurrentMap() {
        if (currentMap != null) return currentMap;
        if (currentMapByName != null) {
            try {
                return MapAsset.valueOf(currentMapByName);
            } catch (IllegalArgumentException e) {
                Gdx.app.error("GameState", e.getMessage());
                return null;
            }
        }
        return null;
    }
    @JsonIgnore
    public void setCurrentMap(MapAsset currentMap) {
        this.currentMap = currentMap;
        this.currentMapByName = currentMap != null ? currentMap.name() : null;
    }

    public int getSaveVersion() {
        return saveVersion;
    }

    public void setSaveVersion(int saveVersion) {
        this.saveVersion = saveVersion;
    }

    public String getCurrentMapByName() {
        return currentMapByName;
    }

    public void setCurrentMapByName(String currentMapByName) {
        this.currentMapByName = currentMapByName;
    }

    public void rebuild() {
        if (getPlayerState() != null) getPlayerState().rebuildItemsByName();
        if (getChestRegistryState() != null) {
            getChestRegistryState().rebuildChestsFromNames();
            getChestRegistryState().getChestsByName().values().forEach(chest -> {
                chest.values().forEach(ChestState::rebuildContentsFromName);
            });
        }
    }
}
