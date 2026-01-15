package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.quest.QuestReward;

public record RewardGrantedEvent(Entity player, String questId, String questTitle, QuestReward reward) {
}
