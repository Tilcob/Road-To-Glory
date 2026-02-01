package com.github.tilcob.game.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;

import java.util.HashMap;
import java.util.Map;

public class SkillTreeLoader {
    private static final Map<String, SkillTreeDefinition> DEFINITIONS = new HashMap<>();

    public static void loadAll() {
        Json json = new Json();
        FileHandle dir = Gdx.files.internal("data/skills");
        if (!dir.exists())
            return;

        for (FileHandle entry : dir.list(".json")) {
            try {
                SkillTreeDefinition def = json.fromJson(SkillTreeDefinition.class, entry);
                DEFINITIONS.put(def.getId(), def);
                Gdx.app.log("SkillTreeLoader", "Loaded skill tree: " + def.getId());
            } catch (Exception e) {
                Gdx.app.error("SkillTreeLoader", "Failed to load skill tree: " + entry.name(), e);
            }
        }
    }

    public static SkillTreeDefinition get(String id) {
        return DEFINITIONS.get(id);
    }
}
