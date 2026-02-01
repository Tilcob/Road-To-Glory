package com.github.tilcob.game.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;

import java.util.HashMap;
import java.util.Map;

public class SkillTreeLoader {
    private static final Map<String, SkillTreeDefinition> DEFINITIONS = new HashMap<>();
    private static final String TAG = SkillTreeLoader.class.getSimpleName();

    public static void loadAll() {
        FileHandle dir = Gdx.files.internal("skill-trees");
        if (!dir.exists()) {
            String msg = "Skills directory not found: " + dir.path();
            if (Constants.DEBUG) throw new IllegalStateException(msg);
            Gdx.app.error(TAG, msg);
            return;
        }
        FileHandle indexFile = dir.child("index.json");
        if (!indexFile.exists()) {
            String msg = "Skills index not found: " + indexFile.path();
            if (Constants.DEBUG) throw new IllegalStateException(msg);
            Gdx.app.error(TAG, msg);
            return;
        }

        JsonReader reader = new JsonReader();
        JsonValue index = reader.parse(indexFile);
        if (!index.isArray()) {
            String msg = "Skills index must be a JSON array: " + indexFile.path();
            if (Constants.DEBUG) throw new IllegalArgumentException(msg);
            Gdx.app.error(TAG, msg);
            return;
        }

        Json json = new Json();

        for (JsonValue entry = index.child; entry != null; entry = entry.next) {
            String fileName = entry.asString();
            if (fileName == null || fileName.isBlank()) {
                String msg = "Index entry must be a non-empty string: " + indexFile.path();
                if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                Gdx.app.error(TAG, msg);
                continue;
            }

            FileHandle file = dir.child(fileName);
            if (!file.exists()) {
                String msg = "Skill file missing: " + file.path();
                if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                Gdx.app.error(TAG, msg);
                continue;
            }

            try {
                SkillTreeDefinition def = json.fromJson(SkillTreeDefinition.class, file);

                if (DEFINITIONS.containsKey(def.getId())) {
                    String msg = "Duplicate skill tree id '" + def.getId() + "' in " + file.path();
                    if (Constants.DEBUG) throw new IllegalArgumentException(msg);
                    Gdx.app.error(TAG, msg);
                    continue;
                }
                DEFINITIONS.put(def.getId(), def);
                Gdx.app.log(TAG, "Loaded skill tree: " + def.getId());
            } catch (Exception e) {
                String msg = "Failed to load skill tree: " + file.path();
                if (Constants.DEBUG) throw new RuntimeException(msg, e);
                Gdx.app.error(TAG, msg, e);
            }
        }
    }

    public static SkillTreeDefinition get(String id) {
        return DEFINITIONS.get(id);
    }
}
