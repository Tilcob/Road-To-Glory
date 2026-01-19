package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.quest.QuestLifecycleService;

public class RewardSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestLifecycleService questLifecycleService;

    public RewardSystem(GameEventBus eventBus, QuestLifecycleService questLifecycleService) {
        this.eventBus = eventBus;
        this.questLifecycleService = questLifecycleService;

        eventBus.subscribe(QuestRewardEvent.class, this::onQuestReward);
    }

    private void onQuestReward(QuestRewardEvent event) {
        questLifecycleService.claimReward(event.player(), event.questId());
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(QuestRewardEvent.class, this::onQuestReward);
    }
}
