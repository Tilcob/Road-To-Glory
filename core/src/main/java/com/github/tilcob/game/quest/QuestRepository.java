package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tilcob.game.event.GameEventBus;

import java.io.IOException;
import java.util.*;

public class QuestRepository {
    private static final String TAG = QuestRepository.class.getSimpleName();

    private final GameEventBus eventBus;
    private final boolean useManifest;
    private final String manifestPath;
    private final String questsDir;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, QuestJson> definitions = new HashMap<>();

    public QuestRepository(GameEventBus eventBus, boolean useManifest, String manifestPath, String questsDir) {
        this.eventBus = eventBus;
        this.useManifest = useManifest;
        this.manifestPath = manifestPath;
        this.questsDir = questsDir;
    }

    public Map<String, QuestJson> loadAll() {
        definitions.clear();
        List<FileHandle> questFiles = useManifest ? loadManifestFiles() : loadDirectoryFiles();
        for (FileHandle questFile : questFiles) {
            loadQuestFile(questFile);
        }
        return Collections.unmodifiableMap(definitions);
    }

    public QuestJson getQuestDefinition(String questId) {
        return definitions.get(questId);
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    private List<FileHandle> loadManifestFiles() {
        FileHandle manifest = Gdx.files.internal(manifestPath);
        if (!manifest.exists()) {
            Gdx.app.error(TAG, "Quest manifest not found: " + manifestPath);
            return List.of();
        }
        try {
            String[] entries = mapper.readValue(manifest.readString(), String[].class);
            List<FileHandle> files = new ArrayList<>();
            for (String entry : entries) {
                FileHandle questFile = resolveQuestFile(entry);
                if (!questFile.exists()) {
                    Gdx.app.error(TAG, "Quest file from index not found: " + questFile.path());
                    continue;
                }
                files.add(questFile);
            }
            return files;
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read quest manifest: " + manifestPath, e);
            return List.of();
        }
    }

    private List<FileHandle> loadDirectoryFiles() {
        FileHandle directory = Gdx.files.internal(questsDir);
        if (!directory.exists() || !directory.isDirectory()) {
            Gdx.app.error(TAG, "Quest directory not found or not a directory: " + questsDir);
            return List.of();
        }
        FileHandle[] files = directory.list();
        if (files == null) {
            return List.of();
        }
        List<FileHandle> questFiles = new ArrayList<>();
        for (FileHandle file : files) {
            if ("json".equalsIgnoreCase(file.extension())) {
                questFiles.add(file);
            }
        }
        return questFiles;
    }

    private FileHandle resolveQuestFile(String entry) {
        if (entry.contains("/")) {
            return Gdx.files.internal(entry);
        }
        return Gdx.files.internal(questsDir + "/" + entry);
    }

    private void loadQuestFile(FileHandle questFile) {
        QuestJson questJson;
        try {
            questJson = mapper.readValue(questFile.readString(), QuestJson.class);
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read quest file: " + questFile.path(), e);
            return;
        }

        if (questJson.questId() == null || questJson.questId().isBlank()) {
            Gdx.app.error(TAG, "Missing questId in quest file: " + questFile.path());
            return;
        }

        if (definitions.containsKey(questJson.questId())) {
            Gdx.app.error(TAG, "Duplicate questId detected: " + questJson.questId());
            return;
        }

        definitions.put(questJson.questId(), questJson);
    }
}
