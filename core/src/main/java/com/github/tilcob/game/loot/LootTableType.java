package com.github.tilcob.game.loot;

import java.util.function.Supplier;

public enum LootTableType {
    BASIC_CHEST(BasicChestLootTable::new),;

    private final Supplier<LootTable> supplier;

    LootTableType(Supplier<LootTable> supplier) {
        this.supplier = supplier;
    }

    public LootTable getLootTable() {
        return supplier.get();
    }
}
