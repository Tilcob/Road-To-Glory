package com.github.tilcob.game.loot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class BasicChestLootTable implements LootTable {

    @Override
    public Array<String> roll() {
        Array<String> loot = new Array<>();

        if (MathUtils.randomBoolean(.8f)) loot.add("boots");
        if (MathUtils.randomBoolean(.5f)) loot.add("helmet");
        if (MathUtils.randomBoolean(.1f)) loot.add("boots");

        return loot;
    }
}
