package com.github.tilcob.game.loot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.item.ItemType;

public class BasicChestLootTable implements LootTable {

    @Override
    public Array<ItemType> roll() {
        Array<ItemType> loot = new Array<>();

        if (MathUtils.randomBoolean(.8f)) loot.add(ItemType.BOOTS);
        if (MathUtils.randomBoolean(.5f)) loot.add(ItemType.HELMET);
        if (MathUtils.randomBoolean(.1f)) loot.add(ItemType.BOOTS);

        return loot;
    }
}
