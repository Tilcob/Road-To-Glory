package com.github.tilcob.game.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.PlayerState;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;
import com.github.tilcob.game.save.states.chest.ChestState;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest extends HeadlessGdxTest {

    @Test
    void saveAndLoadRoundTripRebuildsState() throws Exception {
        FileHandle saveFile = Gdx.files.local("tests/build/test-saves/savegame.sav");
        saveFile.file().getParentFile().mkdirs();
        saveFile.delete();

        SaveManager saveManager = new SaveManager(saveFile.path());

        GameState state = new GameState();
        PlayerState playerState = new PlayerState();
        playerState.setPosX(10f);
        playerState.setPosY(20f);
        playerState.setItemsByName(List.of("sword", "boots"));
        state.setPlayerState(playerState);

        ChestRegistryState chestRegistryState = new ChestRegistryState();
        ChestState chestState = new ChestState();
        chestState.setContentsByName(List.of("armor"));

        Map<String, Map<Integer, ChestState>> chestsByName = new HashMap<>();
        Map<Integer, ChestState> chestMap = new HashMap<>();
        chestMap.put(1, chestState);
        chestsByName.put(MapAsset.MAIN.name(), chestMap);
        chestRegistryState.setChestsByName(chestsByName);
        state.setChestRegistryState(chestRegistryState);

        saveManager.save(state);
        GameState loaded = saveManager.load();

        assertEquals(10f, loaded.getPlayerState().getPosX());
        assertTrue(loaded.getPlayerState().getItems().contains("sword"));
        assertTrue(loaded.getChestRegistryState().getChests().containsKey(MapAsset.MAIN));
        assertTrue(loaded.getChestRegistryState().getChests().get(MapAsset.MAIN).get(1)
            .getContents().contains("armor"));
    }

    @Test
    void saveThrowsWhenRequiredFieldsAreMissing() {
        SaveManager saveManager = new SaveManager("tests/build/test-saves/invalid-save.sav");

        GameState state = new GameState();
        PlayerState playerState = new PlayerState();
        playerState.setItemsByName(null);
        state.setPlayerState(playerState);
        state.setChestRegistryState(new ChestRegistryState());

        assertThrows(IllegalStateException.class, () -> saveManager.save(state));
    }

    @Test
    void loadThrowsWhenSaveFileIsMissing() {
        FileHandle saveFile = Gdx.files.local("tests/build/test-saves/missing-save.sav");
        saveFile.delete();

        SaveManager saveManager = new SaveManager(saveFile.path());

        assertThrows(java.io.IOException.class, saveManager::load);
    }

    @Test
    void saveCreatesRotatingBackupsOnOverwrite() throws Exception {
        FileHandle saveFile = Gdx.files.local("tests/build/test-saves/backup-test.sav");
        saveFile.file().getParentFile().mkdirs();
        saveFile.delete();

        SaveManager saveManager = new SaveManager(saveFile.path());

        GameState first = new GameState();
        PlayerState ps1 = new PlayerState();
        ps1.setPosX(1f);
        ps1.setPosY(2f);
        ps1.setItemsByName(List.of("sword"));
        first.setPlayerState(ps1);
        ChestRegistryState cr1 = new ChestRegistryState();
        cr1.setChestsByName(new HashMap<>());
        first.setChestRegistryState(cr1);

        saveManager.save(first);

        GameState second = new GameState();
        PlayerState ps2 = new PlayerState();
        ps2.setPosX(3f);
        ps2.setPosY(4f);
        ps2.setItemsByName(List.of("boots"));
        second.setPlayerState(ps2);
        ChestRegistryState cr2 = new ChestRegistryState();
        cr2.setChestsByName(new HashMap<>());
        second.setChestRegistryState(cr2);

        saveManager.save(second);

        FileHandle backupDir = saveFile.parent().child("backups").child(saveFile.nameWithoutExtension());
        assertTrue(backupDir.exists(), "Backup directory should exist");

        FileHandle last = backupDir.child("last.sav");
        assertTrue(last.exists(), "last.sav should exist after overwriting a save");

        FileHandle[] rotating = backupDir.list((dir, name) -> name.endsWith(".sav") && !name.equals("last.sav"));
        assertNotNull(rotating);
        assertTrue(rotating.length >= 1, "At least one rotating backup should exist");

        String stateJson = readStateJsonFromSav(last);
        assertTrue(stateJson.contains("\"posX\""));
        assertTrue(stateJson.contains("1.0") || stateJson.contains("1"), "Backup should contain first state's data");
    }

    @Test
    void loadRestoresFromBackupWhenMainSaveIsCorrupted() throws Exception {
        FileHandle saveFile = Gdx.files.local("tests/build/test-saves/corrupt-restore.sav");
        saveFile.file().getParentFile().mkdirs();
        saveFile.delete();

        SaveManager saveManager = new SaveManager(saveFile.path());

        GameState stateA = new GameState();
        PlayerState psA = new PlayerState();
        psA.setPosX(1f);
        psA.setPosY(2f);
        psA.setItemsByName(List.of("sword"));
        stateA.setPlayerState(psA);

        ChestRegistryState crA = new ChestRegistryState();
        Map<String, Map<Integer, ChestState>> chestsByNameA = new HashMap<>();
        chestsByNameA.put(MapAsset.MAIN.name(), new HashMap<>());
        crA.setChestsByName(chestsByNameA);
        stateA.setChestRegistryState(crA);

        saveManager.save(stateA);

        GameState stateB = new GameState();
        PlayerState psB = new PlayerState();
        psB.setPosX(9f);
        psB.setPosY(9f);
        psB.setItemsByName(List.of("boots"));
        stateB.setPlayerState(psB);

        ChestRegistryState crB = new ChestRegistryState();
        Map<String, Map<Integer, ChestState>> chestsByNameB = new HashMap<>();
        chestsByNameB.put(MapAsset.MAIN.name(), new HashMap<>());
        crB.setChestsByName(chestsByNameB);
        stateB.setChestRegistryState(crB);
        saveManager.save(stateB);

        saveFile.writeString("THIS_IS_NOT_A_ZIP_SAVE", false);
        GameState loaded = saveManager.load();

        assertEquals(1f, loaded.getPlayerState().getPosX(), "Should restore from backup (State A)");
        assertTrue(loaded.getPlayerState().getItems().contains("sword"));
        assertFalse(loaded.getPlayerState().getItems().contains("boots"));
    }

    private static String readStateJsonFromSav(FileHandle savFile) throws IOException {
        try (InputStream inputStream = savFile.read();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if ("state.json".equals(zipEntry.getName())) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = zipInputStream.read(buffer)) >= 0) {
                        byteArrayOutputStream.write(buffer, 0, read);
                    }
                    return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
                }
                zipInputStream.closeEntry();
            }
        }
        throw new IOException("state.json not found in sav: " + savFile.path());
    }

}
