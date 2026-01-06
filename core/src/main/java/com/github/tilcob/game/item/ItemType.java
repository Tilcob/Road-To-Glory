package com.github.tilcob.game.item;

public enum ItemType {
    UNDEFINED(ItemCategory.UNDEFINED, "", 1),
    HELMET(ItemCategory.HELMET, "helmet", 1),
    SWORD(ItemCategory.WEAPON, "sword", 1),
    BOOTS(ItemCategory.BOOTS, "boots", 1),
    ARMOR(ItemCategory.ARMOR, "armor", 1),
    SHIELD(ItemCategory.SHIELD, "shield", 1),
    RING(ItemCategory.RING, "ring", 1),
    BRACELET(ItemCategory.BRACELET, "bracelet", 1),
    NECKLACE(ItemCategory.NECKLACE, "necklace", 1),
    ;

    private final ItemCategory category;
    private final String drawableName;
    private final int maxStack;

    ItemType(ItemCategory itemCategory, String drawableName, int maxStack) {
        this.category = itemCategory;
        this.drawableName = drawableName;
        this.maxStack = maxStack;
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

    public boolean isStackable() {
        return maxStack > 1;
    }
}
