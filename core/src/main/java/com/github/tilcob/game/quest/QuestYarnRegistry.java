package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuestYarnRegistry {
    private static final String TAG = QuestYarnRegistry.class.getSimpleName();
    private static final String QUESTS_DIR = "quests";

    private final String indexPath;
    private final String indexDir;
    private final Map<String, QuestDefinition> definitions = new HashMap<>();
    private final Map<String, FileHandle> questFiles = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final YarnQuestParser questParser = new YarnQuestParser();

    public QuestYarnRegistry(String indexPath) {
        this(indexPath, QUESTS_DIR);
    }

    public QuestYarnRegistry(String indexPath, String indexDir) {
        this.indexPath = indexPath;
        this.indexDir = indexDir;
    }

    public Map<String, QuestDefinition> loadAll() {
        definitions.clear();
        questFiles.clear();
        FileHandle indexFile = Gdx.files.internal(indexPath);
        if (!indexFile.exists()) {
            Gdx.app.error(TAG, "Quest index not found: " + indexPath);
            return Collections.unmodifiableMap(definitions);
        }

        try {
            JsonNode root = mapper.readTree(indexFile.readString());
            if (!root.isArray()) {
                Gdx.app.error(TAG, "Quest index must be a JSON array: " + indexFile.path());
                return Collections.unmodifiableMap(definitions);
            }
            for (JsonNode entry : root) {
                if (!entry.isTextual()) {
                    Gdx.app.error(TAG, "Invalid quest index entry: " + entry.toString());
                    continue;
                }
                loadQuestDefinition(resolveQuestFileFromEntry(entry.asText()));
            }
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read quest index: " + indexFile.path(), e);
        }
        Gdx.app.debug(TAG, "Loaded " + definitions.size() + " quests");
        return Collections.unmodifiableMap(definitions);
    }

    public QuestDefinition getQuestDefinition(String questId) {
        return definitions.get(questId);
    }

    public Map<String, FileHandle> getQuestFiles() {
        return Collections.unmodifiableMap(questFiles);
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    private void loadQuestDefinition(FileHandle questFile) {
        if (!questFile.exists()) {
            Gdx.app.error(TAG, "Quest file not found: " + questFile.path());
            return;
        }
        QuestDefinition quest = questParser.parse(questFile);
        if (quest == null) return;
        if (definitions.containsKey(quest.questId())) {
            Gdx.app.error(TAG, "Duplicate questId detected: " + quest.questId());
            return;
        }
        definitions.put(quest.questId(), quest);
        questFiles.put(quest.questId(), questFile);
    }

    private FileHandle resolveQuestFileFromEntry(String entry) {
        String fileName = ensureYarnExtension(entry);
        if (fileName.contains("/")) return Gdx.files.internal(fileName);
        return Gdx.files.internal(indexDir + "/" + fileName);
    }

    private String ensureYarnExtension(String entry) {
        if (entry.toLowerCase().endsWith(".yarn")) return entry;
        return entry + ".yarn";
    }
}
