package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.event.RewardGrantedEvent;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestReward;

public class RewardSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestFactory questFactory;

    public RewardSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        this.questFactory = new QuestFactory(eventBus);
        eventBus.subscribe(QuestRewardEvent.class, this::onQuestReward);
    }

    private void onQuestReward(QuestRewardEvent event) {
        Entity player = event.player();
        Quest quest = questFor(event);
        if (quest == null || quest.isRewardClaimed() || !quest.isCompleted()) return;
        quest.setRewardClaimed(true);
        QuestReward reward = quest.getReward();
        applyGold(player, reward);
        applyItems(player, reward);
        if (reward.money() > 0 || !reward.items().isEmpty()) {
            eventBus.fire(new RewardGrantedEvent(player, quest.getQuestId(), quest.getTitle(), reward));
        }
    }

    private Quest questFor(QuestRewardEvent event) {
        QuestLog questLog = QuestLog.MAPPER.get(event.player());
        if (questLog != null) {
            Quest quest = questLog.getQuestById(event.questId());
            if (quest != null) return quest;
        }
        return questFactory.create(event.questId());
    }

    private void applyGold(Entity player, QuestReward reward) {
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
        for (var itemType : reward.items()) {
            inventory.getItemsToAdd().add(itemType);
        }
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(QuestRewardEvent.class, this::onQuestReward);
    }
}
