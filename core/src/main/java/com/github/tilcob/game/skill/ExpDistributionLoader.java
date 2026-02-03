package com.github.tilcob.game.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tilcob.game.config.Constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpDistributionLoader {
    private static final String TAG = ExpDistributionLoader.class.getSimpleName();
    private static final String FILE_PATH = "skill-trees/xp-distribution.json";
    private static final Map<String, Map<String, Float>> DISTRIBUTIONS = new LinkedHashMap<>();

    public static void loadAll() {
        if (!DISTRIBUTIONS.isEmpty()) Gdx.app.log(TAG, "Reloading XP distributions.");
        DISTRIBUTIONS.clear();
        FileHandle file = Gdx.files.internal(FILE_PATH);
        if (!file.exists()) {
            String msg = "XP distribution config not found: " + file.path();
            if (Constants.DEBUG) throw new IllegalStateException(msg);
            Gdx.app.error(TAG, msg);
            return;
        }

        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(file);
        if (root == null || !root.isObject()) {
            String msg = "XP distribution config must be a JSON object: " + file.path();
            if (Constants.DEBUG) throw new IllegalArgumentException(msg);
            Gdx.app.error(TAG, msg);
            return;
        }

        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            String source = entry.name();
            if (source == null || source.isBlank()) {
                String msg = "XP distribution source key must be a non-empty string: " + file.path();
                if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                Gdx.app.error(TAG, msg);
                continue;
            }
            if (!entry.isObject()) {
                String msg = "XP distribution for '" + source + "' must be a JSON object: " + file.path();
                if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                Gdx.app.error(TAG, msg);
                continue;
            }
            Map<String, Float> treeWeights = new LinkedHashMap<>();
            for (JsonValue treeEntry = entry.child; treeEntry != null; treeEntry = treeEntry.next) {
                String treeId = treeEntry.name();
                if (treeId == null || treeId.isBlank()) {
                    String msg = "XP distribution tree id must be a non-empty string: " + file.path();
                    if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                    Gdx.app.error(TAG, msg);
                    continue;
                }
                float weight = treeEntry.asFloat();
                if (weight <= 0f) {
                    String msg = "XP distribution weight must be > 0 for tree '" + treeId + "': " + file.path();
                    if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                    Gdx.app.error(TAG, msg);
                    continue;
                }
                treeWeights.put(treeId, weight);
            }

            if (treeWeights.isEmpty()) {
                String msg = "XP distribution for '" + source + "' is empty: " + file.path();
                if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                Gdx.app.error(TAG, msg);
                continue;
            }
            DISTRIBUTIONS.put(source, Collections.unmodifiableMap(treeWeights));
        }
    }

    public static Map<String, Float> getDistribution(String source) {
        return DISTRIBUTIONS.get(source);
    }
}
