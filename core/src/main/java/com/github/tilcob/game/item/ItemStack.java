package com.github.tilcob.game.item;

public record ItemStack(String itemId, int amount, Integer durability) {
    public ItemStack(String itemId, int amount) {
        this(itemId, amount, null);
    }

}
