package com.github.tilcob.game.cutscene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CutsceneRepository {
    private static final String TAG = CutsceneRepository.class.getSimpleName();
    private static final String MANIFEST_NAME = "index.json";

    private final boolean useManifest;
    private final String cutscenesDir;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, FileHandle> cutsceneFiles = new HashMap<>();

    public CutsceneRepository(boolean useManifest, String cutscenesDir) {
        this.useManifest = useManifest;
        this.cutscenesDir = cutscenesDir;
    }

    public Map<String, FileHandle> loadAll() {
        cutsceneFiles.clear();
        if (useManifest) {
            loadManifestEntries();
        } else {
            loadDirectoryEntries();
        }
        return Collections.unmodifiableMap(cutsceneFiles);
    }

    public FileHandle getCutsceneFile(String cutsceneId) {
        FileHandle fileHandle = cutsceneFiles.get(cutsceneId);
        if (fileHandle != null) return fileHandle;
        FileHandle resolved = Gdx.files.internal(cutscenesDir + "/" + cutsceneId + ".yarn");
        if (resolved.exists()) return resolved;
        Gdx.app.error(TAG, "Cutscene file not found for cutsceneId: " + cutsceneId + " at " + resolved.path());
        return null;
    }

    private void registerCutscene(String cutsceneId, FileHandle fileHandle) {
        if (cutsceneId == null || cutsceneId.isBlank()) {
            Gdx.app.error(TAG, "Cutscene id is missing for file: " + fileHandle.path());
            return;
        }
        if (!fileHandle.exists()) {
            Gdx.app.error(TAG, "Cutscene file not found: " + fileHandle.path());
            return;
        }
        if (cutsceneFiles.containsKey(cutsceneId)) {
            Gdx.app.error(TAG, "Duplicate cutscene id detected: " + cutsceneId);
            return;
        }
        cutsceneFiles.put(cutsceneId, fileHandle);
    }

    private void loadManifestEntries() {
        FileHandle manifest = Gdx.files.internal(cutscenesDir + "/" + MANIFEST_NAME);
        if (!manifest.exists()) {
            Gdx.app.error(TAG, "Cutscene manifest not found: " + manifest.path());
            return;
        }
        try {
            JsonNode root = mapper.readTree(manifest.readString());
            if (root.isArray()) {
                for (JsonNode entry : root) {
                    if (!entry.isTextual()) {
                        Gdx.app.error(TAG, "Invalid cutscene manifest entry: " + entry.toString());
                        continue;
                    }
                    String fileName = entry.asText();
                    String cutsceneId = inferCutsceneId(fileName);
                    registerCutscene(cutsceneId, resolveCutsceneFileFromEntry(fileName));
                }
                return;
            }
            if (root.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    if (!field.getValue().isTextual()) {
                        Gdx.app.error(TAG, "Invalid cutscene manifest entry for " + field.getKey() + ": "
                            + field.getValue().toString());
                        continue;
                    }
                    String fileName = field.getValue().asText();
                    registerCutscene(field.getKey(), resolveCutsceneFileFromEntry(fileName));
                }
                return;
            }
            Gdx.app.error(TAG, "Cutscene manifest must be array or object: " + manifest.path());
        } catch (Exception e) {
            Gdx.app.error(TAG, "Failed to read cutscene manifest: " + manifest.path(), e);
        }
    }

    private void loadDirectoryEntries() {
        FileHandle directory = Gdx.files.internal(cutscenesDir);
        if (!directory.exists() || !directory.isDirectory()) {
            Gdx.app.error(TAG, "Cutscene directory not found or not a directory: " + cutscenesDir);
            return;
        }
        FileHandle[] files = directory.list();
        if (files == null) return;
        for (FileHandle file : files) {
            if ("yarn".equalsIgnoreCase(file.extension())) {
                registerCutscene(file.nameWithoutExtension(), file);
            }
        }
    }

    private FileHandle resolveCutsceneFileFromEntry(String entry) {
        String fileName = ensureYarnExtension(entry);
        if (fileName.contains("/")) return Gdx.files.internal(fileName);
        return Gdx.files.internal(cutscenesDir + "/" + fileName);
    }

    private String inferCutsceneId(String entry) {
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
        if (fileName.toLowerCase().endsWith(".yarn")) return fileName;
        return fileName + ".yarn";
    }
}
