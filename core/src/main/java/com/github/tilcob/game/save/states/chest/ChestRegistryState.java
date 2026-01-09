package com.github.tilcob.game.save.states.chest;


import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tilcob.game.assets.MapAsset;

import java.util.HashMap;
import java.util.Map;

public class ChestRegistryState {
    @JsonIgnore
    private final Map<MapAsset, Map<Integer, ChestState>> chests = new HashMap<>();
    private Map<String, Map<Integer, ChestState>> chestsByName = new HashMap<>();

    public ChestRegistryState() {}

    @JsonIgnore
    public Map<MapAsset, Map<Integer, ChestState>> getChests() {
        return chests;
    }

    @JsonIgnore
    public void rebuildChestsFromNames() {
        chests.clear();
        for (var entry : chestsByName.entrySet()) {
            try {
                MapAsset asset = MapAsset.valueOf(entry.getKey());
                chests.put(asset, entry.getValue());
            } catch (IllegalArgumentException e) {
                Gdx.app.error("ChestRegistryState", "Chest names are not valid");
            }
        }
    }

    public Map<String, Map<Integer, ChestState>> getChestsByName() {
        return chestsByName;
    }

    public void setChestsByName(Map<String, Map<Integer, ChestState>> chestsByName) {
        this.chestsByName = chestsByName;
    }
}
