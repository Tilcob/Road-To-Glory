package com.github.tilcob.game.save.states;


import com.github.tilcob.game.assets.MapAsset;

import java.util.HashMap;
import java.util.Map;

public class ChestRegistryState {
    private Map<MapAsset, Map<Integer, ChestState>> chests = new HashMap<>();

    public ChestRegistryState() {}

    public Map<MapAsset, Map<Integer, ChestState>> getChests() {
        return chests;
    }

    public void setChests(Map<MapAsset, Map<Integer, ChestState>> chests) {
        this.chests = chests;
    }
}
