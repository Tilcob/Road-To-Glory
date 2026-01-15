package com.github.tilcob.game.quest;

import com.github.tilcob.game.item.ItemType;

import java.util.List;

public record QuestReward(int money, List<ItemType> items) {
    public QuestReward(int money, List<ItemType> items) {
        this.money = money;
        this.items = items == null ? List.of() : List.copyOf(items);
    }
}
