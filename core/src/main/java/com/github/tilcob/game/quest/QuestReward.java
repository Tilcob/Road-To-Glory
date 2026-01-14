package com.github.tilcob.game.quest;

import com.github.tilcob.game.item.ItemType;

import java.util.List;

public record QuestReward(int gold, List<ItemType> items) {
    public QuestReward(int gold, List<ItemType> items) {
        this.gold = gold;
        this.items = items == null ? List.of() : List.copyOf(items);
    }
}
