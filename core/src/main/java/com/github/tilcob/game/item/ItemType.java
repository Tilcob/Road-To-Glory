package com.github.tilcob.game.item;

@Deprecated(forRemoval = false)
public enum ItemType {
    UNDEFINED(ItemCategory.UNDEFINED, "", 1, ItemDefinitions.UNDEFINED_ID),
    HELMET(ItemCategory.HELMET, "helmet", 1, "helmet"),
    SWORD(ItemCategory.WEAPON, "sword", 1, "sword"),
    BOOTS(ItemCategory.BOOTS, "boots", 2, "boots"),
    ARMOR(ItemCategory.ARMOR, "armor", 1, "armor"),
    SHIELD(ItemCategory.SHIELD, "shield", 1, "shield"),
    RING(ItemCategory.RING, "ring", 1, "ring"),
    BRACELET(ItemCategory.BRACELET, "bracelet", 1, "bracelet"),
    NECKLACE(ItemCategory.NECKLACE, "necklace", 1, "necklace"),
    ;

    private final ItemCategory category;
    private final String drawableName;
    private final int maxStack;
    private final String itemId;

    ItemType(ItemCategory itemCategory, String drawableName, int maxStack, String itemId) {
        this.category = itemCategory;
        this.drawableName = drawableName;
        this.maxStack = maxStack;
        this.itemId = itemId;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public String getDrawableName() {
        return drawableName;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public String getItemId() {
        return itemId;
    }

    public boolean isStackable() {
        return maxStack > 1;
    }
}
