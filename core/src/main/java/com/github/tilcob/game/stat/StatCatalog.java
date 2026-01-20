package com.github.tilcob.game.stat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StatCatalog {
    private static final String INDEX_PATH = "stats/stats.json";
    private static final Map<String, StatKey> STAT_KEYS = new LinkedHashMap<>();
    private static boolean loaded = false;

    private StatCatalog() {
    }

    public static Collection<StatKey> getAll() {
        ensureLoaded();
        return Collections.unmodifiableCollection(STAT_KEYS.values());
    }

    public static boolean isKnown(String id) {
        ensureLoaded();
        return STAT_KEYS.containsKey(id);
    }

    public static StatKey require(String id, FileHandle context) {
        ensureLoaded();
        StatKey key = STAT_KEYS.get(id);
        if (key == null) {
            String location = context == null ? INDEX_PATH : context.path();
            throw new IllegalArgumentException("Unknown stat key '" + id + "' in " + location);
        }
        return key;
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        FileHandle indexFile = Gdx.files.internal(INDEX_PATH);
        if (!indexFile.exists()) {
            throw new IllegalStateException("Stats index not found: " + indexFile.path());
        }
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(indexFile);
        JsonValue entries = resolveEntries(root, indexFile);
        for (JsonValue entry = entries.child; entry != null; entry = entry.next) {
            if (entry.isNull() || entry.asString().isBlank()) {
                throw new IllegalArgumentException("Stat entry must be a non-empty string in " + indexFile.path());
            }
            String id = entry.asString();
            if (STAT_KEYS.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate stat key '" + id + "' in " + indexFile.path());
            }
            STAT_KEYS.put(id, new StatKey(id));
        }
        loaded = true;
    }

    private static JsonValue resolveEntries(JsonValue root, FileHandle indexFile) {
        if (root.isArray()) {
            return root;
        }
        JsonValue stats = root.get("stats");
        if (stats != null && stats.isArray()) {
            return stats;
        }
        throw new IllegalArgumentException("Stats index must be a JSON array or contain a 'stats' array: " + indexFile.path());
    }
}
