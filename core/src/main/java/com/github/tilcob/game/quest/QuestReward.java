package com.github.tilcob.game.quest;

import java.util.List;

public record QuestReward(int money, int exp, List<String> items) {
    public QuestReward(int money, int exp, List<String> items) {
        this.money = money;
        this.exp = exp;
        this.items = items == null ? List.of() : List.copyOf(items);
    }
}
