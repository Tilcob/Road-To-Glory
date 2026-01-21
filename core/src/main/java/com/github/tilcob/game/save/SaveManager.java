package com.github.tilcob.game.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tilcob.game.save.registry.MigrationRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.PlayerState;
import com.github.tilcob.game.save.states.chest.ChestRegistryState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SaveManager {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final int CURRENT_VERSION = 4;
    private final FileHandle saveDirectory;
    private FileHandle saveFile;
    private SaveSlot activeSlot;
    private final MigrationRegistry migrationRegistry = new MigrationRegistry();

    public SaveManager(String path) {
        this.saveFile = Gdx.files.local(path);
        this.saveDirectory = saveFile.parent();
        this.activeSlot = null;
    }

    public SaveManager(String directory, SaveSlot saveSlot) {
        this.saveDirectory = Gdx.files.local(directory);
        setActiveSlot(Objects.requireNonNull(saveSlot, "Save slot is required!"));
    }

    public void save(GameState gameState) throws IOException {
        gameState.setSaveVersion(CURRENT_VERSION);
        validate(gameState);
        createBackup();
        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(gameState);
        atomicWrite(json);
    }

    public GameState load() throws IOException {
        if (!saveFile.exists()) {
            throw new IOException("Savegame doesn't exist");
        }
        GameState state = MAPPER.readValue(saveFile.readString(), GameState.class);
        migrationRegistry.migrate(state);
        state.rebuild();
        return state;
    }

    public boolean exists() {
        return saveFile.exists();
    }

    public SaveSlot getActiveSlot() {
        return activeSlot;
    }

    public void setActiveSlot(SaveSlot slot) {
        this.activeSlot = slot;
        this.saveFile = saveDirectory.child(slot.getFileName());
    }

    public List<SaveSlotInfo> listSlots() {
        List<SaveSlotInfo> slots = new ArrayList<>();
        for (SaveSlot slot : SaveSlot.standardSlots()) {
            FileHandle slotFile = saveDirectory.child(slot.getFileName());
            boolean exists = slotFile.exists();
            long lastModified = exists ? slotFile.file().lastModified() : 0L;
            slots.add(new SaveSlotInfo(slot, exists, lastModified));
        }
        return slots;
    }

    private void validate(GameState state) {
        if (state == null) throw new IllegalStateException("GameState is null");

        PlayerState ps = state.getPlayerState();
        if (ps == null) throw new IllegalStateException("PlayerState is null");
        if (Float.isNaN(ps.getPosX()) || Float.isNaN(ps.getPosY())) throw new IllegalStateException("Player position invalid");
        if (ps.getItemsByName() == null) throw new IllegalStateException("ItemsByName is null");

        ChestRegistryState crs = state.getChestRegistryState();
        if (crs == null) throw new IllegalStateException("ChestRegistryState is null");
        if (crs.getChestsByName() == null) throw new IllegalStateException("ChestsByName is null");
    }

    private void atomicWrite(String json) {
        FileHandle tmp = saveDirectory.child("tmp").child(saveFile.name() + ".tmp");
        tmp.file().getParentFile().mkdirs();
        tmp.writeString(json, false);
        tmp.moveTo(saveFile);
    }

    private void createBackup() {
        FileHandle backup = saveDirectory.child("backups").child(saveFile.name() + ".bak");
        backup.file().getParentFile().mkdirs();
        if (saveFile.exists()) {
            saveFile.copyTo(backup);
        }
    }
}
