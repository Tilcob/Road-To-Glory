package com.github.tilcob.game.yarn;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.yarn.script.ScriptComplier;
import com.github.tilcob.game.yarn.script.ScriptEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QuestScriptStore {
    private static final String TAG = QuestScriptStore.class.getSimpleName();

    private final QuestYarnRegistry questYarnRegistry;
    private final Map<String, Map<String, String>> questSignalIndex = new HashMap<>();
    private final Map<String, List<ScriptEvent>> nodes = new HashMap<>();
    private boolean loaded = false;

    public QuestScriptStore(QuestYarnRegistry questYarnRegistry) {
        this.questYarnRegistry = questYarnRegistry;
    }

    public boolean hasNode(String nodeId) {
        ensureLoaded();
        return nodeId != null && nodes.containsKey(nodeId);
    }

    public List<ScriptEvent> get(String nodeId) {
        ensureLoaded();
        return nodeId == null ? null : nodes.get(nodeId);
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        if (questYarnRegistry == null) {
            logError("QuestYarnRegistry is null - cannot load quest nodes.");
            return;
        }

        if (questYarnRegistry.isEmpty()) {
            questYarnRegistry.loadAll();
        }

        Map<String, FileHandle> files = questYarnRegistry.getQuestFiles();
        if (files.isEmpty()) {
            logDebug("No quest files found in registry.");
            return;
        }

        for (Map.Entry<String, FileHandle> e : files.entrySet()) {
            FileHandle fh = e.getValue();
            if (fh == null || !fh.exists()) continue;

            String content;
            try {
                content = fh.readString("UTF-8");
            } catch (Exception ex) {
                logError("Failed reading quest file: " + fh.path(), ex);
                continue;
            }

            List<YarnParser.YarnNodeRaw> rawNodes = YarnParser.parse(content);
            for (YarnParser.YarnNodeRaw raw : rawNodes) {
                String id = raw.id();
                if (id == null || id.isBlank()) continue;

                if (nodes.containsKey(id)) {
                    logError("Duplicate quest node id '" + id + "' (file: " + fh.path() + ")");
                    continue; // first wins
                }

                List<ScriptEvent> compiled = ScriptComplier.compile(raw.bodyLines());
                nodes.put(id, compiled);
                indexQuestSignalNodeId(id);
            }
        }

        logDebug("Loaded quest nodes: " + nodes.size());
    }

    private void indexQuestSignalNodeId(String nodeId) {
        if (nodeId == null) return;
        if (!nodeId.startsWith("q_")) return;

        int onIndex = nodeId.indexOf("_on_");
        if (onIndex <= 2) return;

        String questId = nodeId.substring(2, onIndex);
        String eventType = nodeId.substring(onIndex + 4).toLowerCase();

        if (questId.isBlank() || eventType.isBlank()) return;

        questSignalIndex
            .computeIfAbsent(questId, q -> new HashMap<>())
            .put(eventType, nodeId);
    }

    public String getQuestSignalNodeId(String questId, String eventType) {
        if (questId == null || eventType == null) return null;
        Map<String, String> map = questSignalIndex.get(questId);
        if (map == null) return null;
        return map.get(eventType.toLowerCase());
    }

    private static void logDebug(String msg) {
        if (Gdx.app != null && Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
            Gdx.app.debug(TAG, msg);
        }
    }

    private static void logError(String msg) {
        if (Gdx.app != null) Gdx.app.error(TAG, msg);
    }

    private static void logError(String msg, Throwable t) {
        if (Gdx.app != null) Gdx.app.error(TAG, msg, t);
    }
}
