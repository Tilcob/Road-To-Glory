package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.item.ItemCategory;

public record ItemModel(int itemEntityId, ItemCategory category, String atlasKey, int slotIdx, boolean equipped) {
}
