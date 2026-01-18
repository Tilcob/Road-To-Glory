package com.github.tilcob.game.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemLoader {
    private ItemLoader() {
    }

    public static List<ItemDefinition> loadAll() {
        FileHandle itemsDirectory = Gdx.files.internal("items");
        if (!itemsDirectory.exists()) {
            throw new IllegalStateException("Items directory not found: " + itemsDirectory.path());
        }
        Map<String, ItemDefinition> definitions = new HashMap<>();
        JsonReader reader = new JsonReader();

        // list() is intentionally non-recursive; items are expected directly under /assets/items.
        for (FileHandle file : itemsDirectory.list()) {
            if (!file.extension().equalsIgnoreCase("json")) {
                continue;
            }
            if (file.name().equalsIgnoreCase("index.json")) {
                continue;
            }

            JsonValue root = reader.parse(file);
            String id = requireString(root, "id", file);
            if (definitions.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate item id '" + id + "' in " + file.path());
            }

            String name = requireString(root, "name", file);
            String categoryValue = requireString(root, "category", file);
            ItemCategory category = parseCategory(categoryValue, file);
            int maxStack = requireInt(root, "maxStack", file);
            if (maxStack < 1) {
                throw new IllegalArgumentException("Item maxStack must be >= 1 in " + file.path());
            }
            String icon = requireString(root, "icon", file);
            Map<String, Float> stats = parseStats(root.get("stats"), file);

            definitions.put(id, new ItemDefinition(id, name, category, maxStack, icon, stats));
        }

        return List.copyOf(definitions.values());
    }

    private static String requireString(JsonValue root, String name, FileHandle file) {
        JsonValue value = root.get(name);
        if (value == null || value.isNull() || value.asString().isBlank()) {
            throw new IllegalArgumentException("Missing required field '" + name + "' in " + file.path());
        }
        return value.asString();
    }

    private static int requireInt(JsonValue root, String name, FileHandle file) {
        JsonValue value = root.get(name);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("Missing required field '" + name + "' in " + file.path());
        }
        return value.asInt();
    }

    private static ItemCategory parseCategory(String rawCategory, FileHandle file) {
        try {
            return ItemCategory.valueOf(rawCategory);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown item category '" + rawCategory + "' in " + file.path(), ex);
        }
    }

    private static Map<String, Float> parseStats(JsonValue statsValue, FileHandle file) {
        if (statsValue == null || statsValue.isNull()) {
            return Map.of();
        }
        Map<String, Float> stats = new HashMap<>();
        for (JsonValue stat = statsValue.child; stat != null; stat = stat.next) {
            if (stat.name() == null || stat.name().isBlank()) {
                throw new IllegalArgumentException("Invalid stat entry in " + file.path());
            }
            stats.put(stat.name(), stat.asFloat());
        }
        return stats;
    }
}
