package com.github.tilcob.game.registry;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.component.Chest;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.loot.LootTable;
import com.github.tilcob.game.loot.LootTableType;

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

    public static class ChestState {
        private boolean opened;
        private final Array<ItemType> contents;

        public ChestState(Array<ItemType> initialLoot) {
            this.contents = new Array<>(initialLoot);
        }

        public ChestState(LootTableType type) {
            this.contents = new Array<>(type.getLootTable().roll());
        }

        public boolean isOpened() { return opened; }
        public void open() { opened = true; }
        public void close() { opened = false; }
        public Array<ItemType> getContents() { return contents; }
    }
}
