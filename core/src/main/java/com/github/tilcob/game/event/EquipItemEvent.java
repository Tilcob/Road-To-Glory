package com.github.tilcob.game.event;

import com.github.tilcob.game.item.ItemCategory;

public record EquipItemEvent(ItemCategory category, int fromIndex) {
}
