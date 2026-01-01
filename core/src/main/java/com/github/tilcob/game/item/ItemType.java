package com.github.tilcob.game.item;

public enum ItemType {
    UNDEFINED(ItemCategory.UNDEFINED, ""),
    HELMET(ItemCategory.HELMET, "helmet"),
    SWORD(ItemCategory.WEAPON, "sword"),
    BIG_SWORD(ItemCategory.WEAPON, "sword2"),
    BOOTS(ItemCategory.BOOTS, "boots"),
    ARMOR(ItemCategory.ARMOR, "armor"),
    ;

    private final ItemCategory category;
    private final String atlasKey;

    ItemType(ItemCategory itemCategory, String atlasKey) {
        this.category = itemCategory;
        this.atlasKey = atlasKey;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public String getAtlasKey() {
        return atlasKey;
    }
}
