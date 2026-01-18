package com.github.tilcob.game.save.registry;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;
import com.github.tilcob.game.save.states.chest.ChestState;

import java.util.HashMap;
import java.util.Map;

public class ChestRegistry {
    private final Map<MapAsset, Map<Integer, ChestState>> chests = new HashMap<>();

    public ChestState getOrCreate(MapAsset map, int id, Array<String> loot) {
        return chests
            .computeIfAbsent(map, m -> new HashMap<>())
            .computeIfAbsent(id, i -> new ChestState(loot));
    }

    public boolean contains(MapAsset map, int id) {
        return chests.getOrDefault(map, Map.of()).containsKey(id);
    }

    public void clear(MapAsset map) {
        chests.remove(map);
    }

    public ChestRegistryState toState() {
        ChestRegistryState state = new ChestRegistryState();
        for (var entry : chests.entrySet()) {
            state.getChestsByName().put(entry.getKey().name(), entry.getValue());
        }

        return state;
    }

    public void loadFromState(ChestRegistryState state) {
        if (state == null) return;
        chests.clear();
        for (var entry : state.getChestsByName().entrySet()) {
            MapAsset map = MapAsset.valueOf(entry.getKey());
            chests.put(map, entry.getValue());
        }
    }
}
