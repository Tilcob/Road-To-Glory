package com.github.tilcob.game.quest;

import java.util.List;

public record QuestJson(String questId, String title, String description,
                        List<StepJson> steps, RewardJson rewards) {

    public record StepJson(String type, String npc, String item, int amount, String enemy) {
    }

    public record RewardJson(int money, List<String> items) {
    }
}
