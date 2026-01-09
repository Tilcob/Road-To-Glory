package com.github.tilcob.game.save.migration;

import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;
import com.github.tilcob.game.save.states.chest.ChestState;
import com.github.tilcob.game.save.states.GameState;

import java.util.HashMap;
import java.util.Map;

public class MigrationV3 implements Migration {
    @Override
    public int fromVersion() {
        return 3;
    }

    @Override
    public void migrate(GameState state) {
        ChestRegistryState chestRegistryState = state.getChestRegistryState();
        if (chestRegistryState == null) return;
        if (!chestRegistryState.getChestsByName().isEmpty()) return;

        Map<String, Map<Integer, ChestState>> migrated = new HashMap<>();
        for (var entry : chestRegistryState.getChests().entrySet()) {
            MapAsset mapAsset = entry.getKey();
            String map = mapAsset.name();
            migrated.put(map, entry.getValue());
        }
        chestRegistryState.setChestsByName(migrated);
        chestRegistryState.getChests().clear();
    }
}
