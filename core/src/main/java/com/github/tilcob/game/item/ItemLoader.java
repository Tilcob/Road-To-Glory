package com.github.tilcob.game.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tilcob.game.stat.StatCatalog;
import com.github.tilcob.game.stat.StatKey;

import java.util.ArrayList;
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
        FileHandle indexFile = itemsDirectory.child("index.json");
        if (!indexFile.exists()) {
            throw new IllegalStateException("Items index not found: " + indexFile.path());
        }
        Map<String, ItemDefinition> definitions = new HashMap<>();
        JsonReader reader = new JsonReader();
        JsonValue index = reader.parse(indexFile);
        if (!index.isArray()) {
            throw new IllegalArgumentException("Item index must be a JSON array: " + indexFile.path());
        }

        for (JsonValue entry = index.child; entry != null; entry = entry.next) {
            if (entry.isNull() || entry.asString().isBlank()) {
                throw new IllegalArgumentException("Item index entry must be a non-empty string: " + indexFile.path());
            }
            FileHandle file = itemsDirectory.child(entry.asString());
            if (!file.exists()) throw new IllegalArgumentException("Item file missing: " + file.path());

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
            Map<StatKey, Float> stats = parseStats(root.get("stats"), file);
            List<ItemStatModifier> statModifiers = parseStatModifiers(root.get("statModifiers"), file);

            definitions.put(id, new ItemDefinition(id, name, category, maxStack, icon, stats, statModifiers));
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

    private static Map<StatKey, Float> parseStats(JsonValue statsValue, FileHandle file) {
        if (statsValue == null || statsValue.isNull()) {
            return Map.of();
        }
        Map<StatKey, Float> stats = new HashMap<>();
        for (JsonValue stat = statsValue.child; stat != null; stat = stat.next) {
            if (stat.name() == null || stat.name().isBlank()) {
                throw new IllegalArgumentException("Invalid stat entry in " + file.path());
            }
            StatKey key = StatCatalog.require(stat.name(), file);
            stats.put(key, stat.asFloat());
        }
        return stats;
    }

    private static List<ItemStatModifier> parseStatModifiers(JsonValue modifiersValue, FileHandle file) {
        if (modifiersValue == null || modifiersValue.isNull()) {
            return List.of();
        }
        if (!modifiersValue.isArray()) {
            throw new IllegalArgumentException("Item statModifiers must be a JSON array in " + file.path());
        }
        List<ItemStatModifier> modifiers = new ArrayList<>();
        for (JsonValue entry = modifiersValue.child; entry != null; entry = entry.next) {
            if (!entry.isObject()) {
                throw new IllegalArgumentException("Item statModifiers entry must be an object in " + file.path());
            }
            String statId = entry.getString("stat", "").trim();
            if (statId.isBlank()) {
                throw new IllegalArgumentException("Item statModifiers entry missing 'stat' in " + file.path());
            }
            boolean hasAdditive = entry.has("additive");
            boolean hasMultiplier = entry.has("multiplier");
            if (!hasAdditive && !hasMultiplier) {
                throw new IllegalArgumentException("Item statModifiers entry must include additive or multiplier in " + file.path());
            }
            float additive = hasAdditive ? entry.getFloat("additive") : 0f;
            float multiplier = hasMultiplier ? entry.getFloat("multiplier") : 1f;
            StatKey key = StatCatalog.require(statId, file);
            modifiers.add(new ItemStatModifier(key, additive, multiplier));
        }
        return List.copyOf(modifiers);
    }
}
