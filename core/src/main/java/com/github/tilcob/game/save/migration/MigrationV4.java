package com.github.tilcob.game.save.migration;

import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.PlayerState;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;

public class MigrationV4 implements Migration {

    @Override
    public int fromVersion() {
        return 4;
    }

    @Override
    public void migrate(GameState state) {
        PlayerState playerState = state.getPlayerState();
        if (playerState != null) {
            if (playerState.getItemsByName().isEmpty()) {
                for (var item : playerState.getItems()) {
                    playerState.getItemsByName().add(ItemDefinitionRegistry.resolveId(item));
                }
            } else {
                playerState.setItemsByName(normalizeIds(playerState.getItemsByName()));
            }
        }

        ChestRegistryState chestRegistryState = state.getChestRegistryState();
        if (chestRegistryState != null) {
            for (var chestMap : chestRegistryState.getChests().values()) {
                for (var chest : chestMap.values()) {
                    if (chest.getContentsByName().isEmpty()) {
                        for (var item : chest.getContents()) {
                            chest.getContentsByName().add(ItemDefinitionRegistry.resolveId(item));
                        }
                    }  else {
                    chest.setContentsByName(normalizeIds(chest.getContentsByName()));
                }
                }
            }
        }
    }

    private java.util.List<String> normalizeIds(java.util.List<String> rawIds) {
        java.util.List<String> normalized = new java.util.ArrayList<>();
        for (String raw : rawIds) {
            String resolved = ItemDefinitionRegistry.resolveId(raw);
            if (ItemDefinitionRegistry.isKnownId(resolved)) {
                normalized.add(resolved);
            }
        }
        return normalized;
    }
}
