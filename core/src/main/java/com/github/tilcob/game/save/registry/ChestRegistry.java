package com.github.tilcob.game.save.registry;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.save.states.ChestState;
import java.util.HashMap;
import java.util.Map;

public class ChestRegistry {
    private final Map<MapAsset, Map<Integer, ChestState>> chests = new HashMap<>();

    public ChestState getOrCreate(MapAsset map, int id, Array<ItemType> loot) {
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
}
