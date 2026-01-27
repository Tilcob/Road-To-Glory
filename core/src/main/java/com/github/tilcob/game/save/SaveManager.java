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

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SaveManager {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final int CURRENT_VERSION = 4;
    private static final int SAV_FORMAT_VERSION = 1;
    private static final int BACKUP_ROTATION_LIMIT = 20;
    private static final DateTimeFormatter BACKUP_TIME_STAMP =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

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

        byte[] stateJson = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(gameState);
        FileHandle tmp = tmpFileFor(saveFile);
        writeSavZip(tmp, stateJson);
        createBackups();
        tmp.moveTo(saveFile);
    }

    public GameState load() throws IOException {
        if (!saveFile.exists()) throw new IOException("Savegame doesn't exist");

        try {
            return loadFrom(saveFile);
        } catch (IOException primaryFail) {
            FileHandle restored = tryRestoreFromBackup();
            if (restored != null) {
                restored.copyTo(saveFile);
                return loadFrom(saveFile);
            }
            throw primaryFail;
        }
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

    private GameState loadFrom(FileHandle save) throws IOException {
        if (!save.exists()) throw new IOException("Savegame doesn't exist: " + save.path());
        SavRead read = readSavZip(save);

        String expected = read.meta.stateSha256;
        String actual = sha256Hex(read.stateJson);
        if (expected != null && !expected.isBlank() && !expected.equalsIgnoreCase(actual)) {
            throw new IOException("Save integrity check failed (sha mismatch) for: " + save.name());
        }

        GameState state = MAPPER.readValue(read.stateJson, GameState.class);
        migrationRegistry.migrate(state);
        state.rebuild();
        return state;
    }

    private FileHandle tmpFileFor(FileHandle target) {
        FileHandle tmp = saveDirectory.child("tmp").child(target.name() + ".tmp");
        tmp.file().getParentFile().mkdirs();
        return tmp;
    }

    private void writeSavZip(FileHandle tmpSave, byte[] stateJson) throws IOException {
        SavMeta meta = new SavMeta();
        meta.format = SAV_FORMAT_VERSION;
        meta.savedAt = LocalDateTime.now().toString();
        meta.gameVersion = System.getProperty("game.version", "unknown");
        meta.stateSha256 = sha256Hex(stateJson);
        meta.stateBytes = stateJson.length;

        byte[] metaJson = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(meta);

        try (OutputStream outputStream = tmpSave.write(false)) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            ZipEntry metaEntry = new ZipEntry("meta.json");
            zipOutputStream.putNextEntry(metaEntry);
            zipOutputStream.write(metaJson);
            zipOutputStream.closeEntry();

            ZipEntry stateEntry = new ZipEntry("state.json");
            zipOutputStream.putNextEntry(stateEntry);
            zipOutputStream.write(stateJson);
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        }
    }

    private SavRead readSavZip(FileHandle save) throws IOException {
        SavMeta meta = null;
        byte[] state = null;

        try (InputStream inputStream = save.read();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry e;
            while ((e = zipInputStream.getNextEntry()) != null) {
                if (e.isDirectory()) continue;

                if ("meta.json".equals(e.getName())) {
                    byte[] bytes = readAllBytes(zipInputStream);
                    meta = MAPPER.readValue(bytes, SavMeta.class);
                } else if ("state.json".equals(e.getName())) {
                    state = readAllBytes(zipInputStream);
                }
                zipInputStream.closeEntry();
            }
        }

        if (meta == null) throw new IOException("Invalid save: meta.json missing (" + save.name() + ")");
        if (state == null) throw new IOException("Invalid save: state.json missing (" + save.name() + ")");
        return new SavRead(meta, state);
    }

    private void createBackups() {
        if (!saveFile.exists()) return;

        FileHandle slotBackupDir = backupsDirForActive();
        slotBackupDir.file().mkdirs();

        FileHandle last = slotBackupDir.child("last.sav");
        saveFile.copyTo(last);

        String ts = LocalDateTime.now().format(BACKUP_TIME_STAMP);
        FileHandle rotating = slotBackupDir.child(ts + ".sav");
        saveFile.copyTo(rotating);

        rotateBackups(slotBackupDir, BACKUP_ROTATION_LIMIT);
    }

    private void rotateBackups(FileHandle slotBackupDir, int keepLastN) {
        FileHandle[] files = slotBackupDir.list((dir, name) ->
            name.endsWith(".sav") && !name.equals("last.sav")
        );

        Arrays.sort(files, Comparator.comparingLong((FileHandle f) ->
            f.file().lastModified()).reversed());

        for (int i = keepLastN; i < files.length; i++) {
            files[i].delete();
        }
    }

    private FileHandle backupsDirForActive() {
        String baseName = saveFile.nameWithoutExtension();
        return saveDirectory.child("backups").child(baseName);
    }

    private FileHandle tryRestoreFromBackup() {
        FileHandle directory = backupsDirForActive();
        if (!directory.exists()) return null;

        FileHandle lastBackup = directory.child("last.sav");
        if (lastBackup.exists()) return lastBackup;

        FileHandle[] backups = directory.list((dir, name) -> name.endsWith(".sav"));
        if (backups == null || backups.length == 0) return null;

        Arrays.sort(backups, Comparator.comparingLong((FileHandle f) ->
            f.file().lastModified()).reversed());
        return backups[0];
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

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) >= 0) {
            byteArrayOutputStream.write(buffer, 0, read);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private record SavRead(SavMeta meta, byte[] stateJson) { }

    private static final class SavMeta {
        public int format;
        public String savedAt;
        public String gameVersion;
        public String stateSha256;
        public int stateBytes;
    }
}
