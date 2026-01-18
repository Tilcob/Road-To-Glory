package com.github.tilcob.game.quest;

import com.github.tilcob.game.item.ItemType;

import java.util.List;

public record QuestReward(int money, List<String> items) {
    public QuestReward(int money, List<String> items) {
        this.money = money;
        this.items = items == null ? List.of() : List.copyOf(items);
    }
}
