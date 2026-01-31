package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public final class AssetIndexLoader {
    private AssetIndexLoader() {
    }

    public static List<FileHandle> loadFiles(String dir) {
        if (dir == null || dir.isBlank()) {
            throw new IllegalArgumentException("Asset dir must be non-empty");
        }

        FileHandle indexFile = Gdx.files.internal(dir + "/index.json");
        if (!indexFile.exists()) {
            throw new IllegalStateException("Asset index not found: " + indexFile.path());
        }

        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(indexFile);
        if (!root.isArray()) {
            throw new IllegalArgumentException("Asset index must be a JSON array: " + indexFile.path());
        }

        List<FileHandle> out = new ArrayList<>();
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            if (entry.isNull() || entry.asString().isBlank()) {
                throw new IllegalArgumentException("Asset index entry must be a non-empty string: " + indexFile.path());
            }

            String rel = entry.asString();
            FileHandle file = Gdx.files.internal(dir + "/" + rel);
            if (!file.exists()) {
                throw new IllegalArgumentException("Asset file missing: " + file.path() + " (from " + indexFile.path() + ")");
            }
            out.add(file);
        }

        return List.copyOf(out);
    }
}
