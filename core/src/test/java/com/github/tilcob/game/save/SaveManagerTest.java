package com.github.tilcob.game.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.PlayerState;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;
import com.github.tilcob.game.save.states.chest.ChestState;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest extends HeadlessGdxTest {

    @Test
    void saveAndLoadRoundTripRebuildsState() throws Exception {
        FileHandle saveFile = Gdx.files.local("build/test-saves/savegame.json");
        saveFile.file().getParentFile().mkdirs();
        saveFile.delete();

        SaveManager saveManager = new SaveManager(saveFile.path());

        GameState state = new GameState();
        PlayerState playerState = new PlayerState();
        playerState.setPosX(10f);
        playerState.setPosY(20f);
        playerState.setItemsByName(List.of(ItemType.SWORD.name(), ItemType.BOOTS.name()));
        state.setPlayerState(playerState);

        ChestRegistryState chestRegistryState = new ChestRegistryState();
        ChestState chestState = new ChestState();
        chestState.setContentsByName(List.of(ItemType.ARMOR.name()));

        Map<String, Map<Integer, ChestState>> chestsByName = new HashMap<>();
        Map<Integer, ChestState> chestMap = new HashMap<>();
        chestMap.put(1, chestState);
        chestsByName.put(MapAsset.MAIN.name(), chestMap);
        chestRegistryState.setChestsByName(chestsByName);
        state.setChestRegistryState(chestRegistryState);

        saveManager.save(state);
        GameState loaded = saveManager.load();

        assertEquals(10f, loaded.getPlayerState().getPosX());
        assertTrue(loaded.getPlayerState().getItems().contains(ItemType.SWORD));
        assertTrue(loaded.getChestRegistryState().getChests().containsKey(MapAsset.MAIN));
        assertTrue(loaded.getChestRegistryState().getChests().get(MapAsset.MAIN).get(1)
            .getContents().contains(ItemType.ARMOR));
    }

    @Test
    void saveThrowsWhenRequiredFieldsAreMissing() {
        SaveManager saveManager = new SaveManager("build/test-saves/invalid-save.json");

        GameState state = new GameState();
        PlayerState playerState = new PlayerState();
        playerState.setItemsByName(null);
        state.setPlayerState(playerState);
        state.setChestRegistryState(new ChestRegistryState());

        assertThrows(IllegalStateException.class, () -> saveManager.save(state));
    }

    @Test
    void loadThrowsWhenSaveFileIsMissing() {
        FileHandle saveFile = Gdx.files.local("build/test-saves/missing-save.json");
        saveFile.delete();

        SaveManager saveManager = new SaveManager(saveFile.path());

        assertThrows(java.io.IOException.class, saveManager::load);
    }
}
