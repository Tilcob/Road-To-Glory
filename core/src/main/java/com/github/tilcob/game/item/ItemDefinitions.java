package com.github.tilcob.game.item;

import java.util.HashMap;
import java.util.Map;

public final class ItemDefinitions {
    public static final String UNDEFINED_ID = "undefined";
    private static final Map<String, ItemDefinition> DEFINITIONS;
    private static final Map<String, String> LEGACY_NAME_TO_ID;

    static {
        Map<String, ItemDefinition> definitions = new HashMap<>();
        register(definitions, new ItemDefinition(UNDEFINED_ID, "Undefined", ItemCategory.UNDEFINED, 1, ""));
        register(definitions, new ItemDefinition("helmet", "Helmet", ItemCategory.HELMET, 1, "helmet"));
        register(definitions, new ItemDefinition("sword", "Sword", ItemCategory.WEAPON, 1, "sword"));
        register(definitions, new ItemDefinition("boots", "Boots", ItemCategory.BOOTS, 2, "boots"));
        register(definitions, new ItemDefinition("armor", "Armor", ItemCategory.ARMOR, 1, "armor"));
        register(definitions, new ItemDefinition("shield", "Shield", ItemCategory.SHIELD, 1, "shield"));
        register(definitions, new ItemDefinition("ring", "Ring", ItemCategory.RING, 1, "ring"));
        register(definitions, new ItemDefinition("bracelet", "Bracelet", ItemCategory.BRACELET, 1, "bracelet"));
        register(definitions, new ItemDefinition("necklace", "Necklace", ItemCategory.NECKLACE, 1, "necklace"));
        DEFINITIONS = Map.copyOf(definitions);

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
        LEGACY_NAME_TO_ID = Map.copyOf(legacyMap);
    }

    private ItemDefinitions() {
    }

    private static void register(Map<String, ItemDefinition> definitions, ItemDefinition definition) {
        definitions.put(definition.id(), definition);
    }

    public static ItemDefinition get(String id) {
        return DEFINITIONS.getOrDefault(resolveId(id), DEFINITIONS.get(UNDEFINED_ID));
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
        String legacy = LEGACY_NAME_TO_ID.get(rawId);
        if (legacy != null) {
            return legacy;
        }
        legacy = LEGACY_NAME_TO_ID.get(rawId.toUpperCase());
        if (legacy != null) {
            return legacy;
        }
        return rawId;
    }
}
