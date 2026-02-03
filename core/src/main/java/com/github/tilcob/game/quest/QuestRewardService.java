package com.github.tilcob.game.quest;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.event.ExpGainRequestEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.RewardGrantedEvent;
import com.github.tilcob.game.event.UpdateQuestLogEvent;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

public class QuestRewardService {
    private final GameEventBus eventBus;
    private final QuestFactory questFactory;

    public QuestRewardService(GameEventBus eventBus, QuestYarnRegistry questYarnRegistry) {
        this.eventBus = eventBus;
        this.questFactory = new QuestFactory(questYarnRegistry);
    }

    public void claimReward(Entity player, String questId) {
        if (player == null || questId == null || questId.isBlank()) return;
        Quest quest = questFor(player, questId);
        if (quest == null || quest.isRewardClaimed() || !quest.isCompleted()) return;
        quest.setRewardClaimed(true);
        QuestReward reward = quest.getReward();
        applyMoney(player, reward);
        applyItems(player, reward);
        applyExp(player, reward);
        if (reward.money() > 0 || reward.exp() > 0 || !reward.items().isEmpty()) {
            eventBus.fire(new RewardGrantedEvent(player, quest.getQuestId(), quest.getTitle(), reward));
        }
        eventBus.fire(new UpdateQuestLogEvent(player));
    }

    private Quest questFor(Entity player, String questId) {
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog != null) {
            Quest quest = questLog.getQuestById(questId);
            if (quest != null) return quest;
        }
        return questFactory.create(questId);
    }

    private void applyMoney(Entity player, QuestReward reward) {
        if (reward.money() <= 0) return;
        Wallet wallet = Wallet.MAPPER.get(player);
        if (wallet == null) {
            wallet = new Wallet();
            player.add(wallet);
        }
        wallet.earn(reward.money());
    }

    private void applyItems(Entity player, QuestReward reward) {
        if (reward.items().isEmpty()) return;
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) {
            inventory = new Inventory();
            player.add(inventory);
        }
        for (var itemId : reward.items()) {
            inventory.getItemsToAdd().add(ItemDefinitionRegistry.resolveId(itemId));
        }
    }

    private void applyExp(Entity player, QuestReward reward) {
        if (reward.exp() <= 0) return;
        eventBus.fire(new ExpGainRequestEvent(player, "quest", "quest", 1f, reward.exp()));
    }
}
