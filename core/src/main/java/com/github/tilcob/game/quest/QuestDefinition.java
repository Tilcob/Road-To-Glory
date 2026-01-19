package com.github.tilcob.game.quest;

import java.util.List;

public record QuestDefinition(
    String questId,
    String displayName,
    String journalText,
    String startNode,
    List<StepDefinition> steps,
    RewardTiming rewardTiming,
    RewardDefinition reward
) {
    public record StepDefinition(String type, String npc, String itemId, int amount, String enemy) {
    }

    public record RewardDefinition(int money, List<String> items) {
    }
}
