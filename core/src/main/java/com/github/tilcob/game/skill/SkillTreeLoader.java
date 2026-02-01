package com.github.tilcob.game.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.github.tilcob.game.assets.AssetIndexLoader;
import com.github.tilcob.game.skill.data.SkillTreeDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeLoader {
    private static final Map<String, SkillTreeDefinition> DEFINITIONS = new HashMap<>();

    public static void loadAll() {
        Json json = new Json();
        try {
            List<FileHandle> files = AssetIndexLoader.loadFiles("skills");
            for (FileHandle entry : files) {
                try {
                    SkillTreeDefinition def = json.fromJson(SkillTreeDefinition.class, entry);
                    DEFINITIONS.put(def.getId(), def);
                    Gdx.app.log("SkillTreeLoader", "Loaded skill tree: " + def.getId());
                } catch (Exception e) {
                    Gdx.app.error("SkillTreeLoader", "Failed to load skill tree: " + entry.name(), e);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("SkillTreeLoader", "Failed to load skills directory", e);
        }
    }

    public static SkillTreeDefinition get(String id) {
        return DEFINITIONS.get(id);
    }
}
