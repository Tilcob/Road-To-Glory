package com.github.tilcob.game.loot;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.item.ItemType;

public interface LootTable {
    Array<ItemType> roll();
}
