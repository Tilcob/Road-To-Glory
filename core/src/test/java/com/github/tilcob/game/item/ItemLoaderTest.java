package com.github.tilcob.game.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemLoaderTest extends HeadlessGdxTest {
    @Test
    void loadAllUsesManifestEntries() {
        FileHandle indexFile = Gdx.files.internal("items/index.json");
        assertNotNull(indexFile);
        assertFalse(indexFile.isDirectory());

        JsonReader reader = new JsonReader();
        JsonValue index = reader.parse(indexFile);
        assertFalse(index.isEmpty());

        Set<String> manifestIds = new HashSet<>();
        for (JsonValue entry = index.child; entry != null; entry = entry.next) {
            FileHandle itemFile = Gdx.files.internal("items/" + entry.asString());
            JsonValue root = reader.parse(itemFile);
            manifestIds.add(root.getString("id"));
        }

        List<ItemDefinition> definitions = ItemLoader.loadAll();
        Set<String> loadedIds = new HashSet<>();
        for (ItemDefinition definition : definitions) {
            loadedIds.add(definition.id());
        }

        assertEquals(manifestIds, loadedIds);
    }
}
