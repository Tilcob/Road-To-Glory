package com.github.tilcob.game.item;

import com.github.tilcob.game.stat.StatKey;

public record ItemStatModifier(StatKey stat, float additive, float multiplier) {

    public ItemStatModifier {
        if (stat == null) {
            throw new IllegalArgumentException("Stat key must be provided!");
        }
    }
}
