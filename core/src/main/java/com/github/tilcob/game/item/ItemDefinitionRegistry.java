package com.github.tilcob.game.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for item definitions only; no item entities are stored here.
 */
public final class ItemDefinitionRegistry {
    public static final String UNDEFINED_ID = "undefined";
    private static final Map<String, ItemDefinition> DEFINITIONS = new HashMap<>();
    private static final Map<String, String> LEGACY_ITEM_ID_MIGRATION;

    static {
        Map<String, String> legacyMap = new HashMap<>();
        legacyMap.put("UNDEFINED", UNDEFINED_ID);
        legacyMap.put("HELMET", "helmet");
        legacyMap.put("SWORD", "sword");
        legacyMap.put("BOOTS", "boots");
        legacyMap.put("ARMOR", "armor");
        legacyMap.put("SHIELD", "shield");
        legacyMap.put("RING", "ring");
        legacyMap.put("BRACELET", "bracelet");
        legacyMap.put("NECKLACE", "necklace");
        LEGACY_ITEM_ID_MIGRATION = Map.copyOf(legacyMap);
    }

    private ItemDefinitionRegistry() {
    }

    public static void register(ItemDefinition definition) {
        if (DEFINITIONS.containsKey(definition.id())) {
            throw new IllegalArgumentException("Item definition already registered: " + definition.id());
        }
        DEFINITIONS.put(definition.id(), definition);
    }

    public static ItemDefinition get(String id) {
        String resolved = resolveId(id);
        ItemDefinition definition = DEFINITIONS.get(resolved);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown item definition id: " + id);
        }
        return definition;
    }

    public static boolean contains(String id) {
        return DEFINITIONS.containsKey(id);
    }

    public static Collection<ItemDefinition> getAll() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static boolean isKnownId(String id) {
        return DEFINITIONS.containsKey(id);
    }

    public static String resolveId(String rawId) {
        if (rawId == null) {
            return UNDEFINED_ID;
        }
        if (DEFINITIONS.containsKey(rawId)) {
            return rawId;
        }
        String legacy = LEGACY_ITEM_ID_MIGRATION.get(rawId);
        if (legacy != null) {
            return legacy;
        }
        legacy = LEGACY_ITEM_ID_MIGRATION.get(rawId.toUpperCase());
        if (legacy != null) {
            return legacy;
        }
        return rawId;
    }
}
