package com.github.tilcob.game.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DialogRepository {
    private static final String TAG = DialogRepository.class.getSimpleName();
    private static final String MANIFEST_NAME = "index.json";

    private final boolean useManifest;
    private final String dialogsDir;
    private final Map<String, String> aliasMap;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, FileHandle> dialogFiles = new HashMap<>();

    public DialogRepository(boolean useManifest, String dialogsDir) {
        this(useManifest, dialogsDir, null);
    }

    public DialogRepository(boolean useManifest, String dialogsDir, Map<String, String> aliasMap) {
        this.useManifest = useManifest;
        this.dialogsDir = dialogsDir;
        this.aliasMap = aliasMap == null ? Map.of() : new HashMap<>(aliasMap);
    }

    public FileHandle resolveDialogFile(String npcId) {
        String fileName = aliasMap.getOrDefault(npcId, npcId);
        return resolveDialogFileFromEntry(fileName);
    }

    public Map<String, FileHandle> loadAll() {
        dialogFiles.clear();
        if (useManifest) {
            loadManifestEntries();
        } else {
            loadDirectoryEntries();
        }
        return Collections.unmodifiableMap(dialogFiles);
    }

    public FileHandle getDialogFile(String npcId) {
        FileHandle fileHandle = dialogFiles.get(npcId);
        if (fileHandle != null) {
            return fileHandle;
        }
        fileHandle = resolveDialogFile(npcId);
        if (fileHandle.exists()) {
            return fileHandle;
        }
        Gdx.app.error(TAG, "Dialog file not found for npcId: " + npcId + " at " + fileHandle.path());
        return null;
    }

    public String getDialogText(String npcId) {
        FileHandle fileHandle = getDialogFile(npcId);
        if (fileHandle == null) {
            return null;
        }
        return fileHandle.readString();
    }

    private void loadManifestEntries() {
        FileHandle manifest = Gdx.files.internal(dialogsDir + "/" + MANIFEST_NAME);
        if (!manifest.exists()) {
            Gdx.app.error(TAG, "Dialog manifest not found: " + manifest.path());
            return;
        }
        try {
            JsonNode root = mapper.readTree(manifest.readString());
            if (root.isArray()) {
                for (JsonNode entry : root) {
                    if (!entry.isTextual()) {
                        Gdx.app.error(TAG, "Invalid dialog manifest entry: " + entry.toString());
                        continue;
                    }
                    String fileName = entry.asText();
                    String npcId = inferNpcId(fileName);
                    registerDialog(npcId, resolveDialogFileFromEntry(fileName));
                }
                return;
            }
            if (root.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    if (!field.getValue().isTextual()) {
                        Gdx.app.error(TAG, "Invalid dialog manifest entry for " + field.getKey() + ": "
                            + field.getValue().toString());
                        continue;
                    }
                    String fileName = field.getValue().asText();
                    registerDialog(field.getKey(), resolveDialogFileFromEntry(fileName));
                }
                return;
            }
            Gdx.app.error(TAG, "Dialog manifest must be array or object: " + manifest.path());
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read dialog manifest: " + manifest.path(), e);
        }
    }

    private void loadDirectoryEntries() {
        FileHandle directory = Gdx.files.internal(dialogsDir);
        if (!directory.exists() || !directory.isDirectory()) {
            Gdx.app.error(TAG, "Dialog directory not found or not a directory: " + dialogsDir);
            return;
        }
        FileHandle[] files = directory.list();
        if (files == null) {
            return;
        }
        for (FileHandle file : files) {
            if ("yarn".equalsIgnoreCase(file.extension())) {
                registerDialog(file.nameWithoutExtension(), file);
            }
        }
    }

    private void registerDialog(String npcId, FileHandle fileHandle) {
        if (npcId == null || npcId.isBlank()) {
            Gdx.app.error(TAG, "Dialog npcId is missing for file: " + fileHandle.path());
            return;
        }
        if (!fileHandle.exists()) {
            Gdx.app.error(TAG, "Dialog file not found: " + fileHandle.path());
            return;
        }
        if (dialogFiles.containsKey(npcId)) {
            Gdx.app.error(TAG, "Duplicate dialog npcId detected: " + npcId);
            return;
        }
        dialogFiles.put(npcId, fileHandle);
    }

    private FileHandle resolveDialogFileFromEntry(String entry) {
        String fileName = ensureYarnExtension(entry);
        if (fileName.contains("/")) {
            return Gdx.files.internal(fileName);
        }
        return Gdx.files.internal(dialogsDir + "/" + fileName);
    }

    private String inferNpcId(String entry) {
        String fileName = entry;
        int slashIndex = entry.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex + 1 < entry.length()) {
            fileName = entry.substring(slashIndex + 1);
        }
        if (fileName.toLowerCase().endsWith(".yarn")) {
            return fileName.substring(0, fileName.length() - ".yarn".length());
        }
        return fileName;
    }

    private String ensureYarnExtension(String fileName) {
        if (fileName.toLowerCase().endsWith(".yarn")) {
            return fileName;
        }
        return fileName + ".yarn";
    }
}
