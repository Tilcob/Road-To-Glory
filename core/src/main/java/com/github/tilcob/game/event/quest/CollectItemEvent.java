package com.github.tilcob.game.event.quest;

import com.github.tilcob.game.item.ItemType;

public record CollectItemEvent(ItemType item, int count) {
}
