package com.github.tilcob.game.save.migration;

import com.github.tilcob.game.save.states.ChestRegistryState;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.PlayerState;

public class MigrationV4 implements Migration {

    @Override
    public int fromVersion() {
        return 4;
    }

    @Override
    public void migrate(GameState state) {
        PlayerState playerState = state.getPlayerState();
        if (playerState != null && playerState.getItemsByName().isEmpty()) {
            for (var item : playerState.getItems()) {
                playerState.getItemsByName().add(item.name());
            }
        }

        ChestRegistryState chestRegistryState = state.getChestRegistryState();
        if (chestRegistryState != null) {
            for (var chestMap : chestRegistryState.getChests().values()) {
                for (var chest : chestMap.values()) {
                    if (chest.getContentsByName().isEmpty()) {
                        for (var item : chest.getContents()) {
                            chest.getContentsByName().add(item.name());
                        }
                    }
                }
            }
        }
    }
}
