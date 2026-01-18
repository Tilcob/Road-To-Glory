package com.github.tilcob.game.save;

import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.save.registry.MigrationRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.PlayerState;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;
import com.github.tilcob.game.save.states.chest.ChestState;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationRegistryTest extends HeadlessGdxTest {

    @Test
    void migrateFillsLegacyNamesAndUpdatesVersion() {
        GameState state = new GameState();
        state.setSaveVersion(1);

        PlayerState playerState = new PlayerState();
        playerState.getItems().add("sword");
        state.setPlayerState(playerState);

        ChestRegistryState chestRegistryState = new ChestRegistryState();
        ChestState chestState = new ChestState();
        chestState.getContents().add("armor");
        Map<Integer, ChestState> chestMap = new HashMap<>();
        chestMap.put(1, chestState);
        chestRegistryState.getChests().put(MapAsset.MAIN, chestMap);
        state.setChestRegistryState(chestRegistryState);

        new MigrationRegistry().migrate(state);

        assertEquals(4, state.getSaveVersion());
        assertTrue(state.getPlayerState().getItemsByName().contains("sword"));
        assertTrue(state.getChestRegistryState().getChestsByName().containsKey(MapAsset.MAIN.name()));
    }
}
