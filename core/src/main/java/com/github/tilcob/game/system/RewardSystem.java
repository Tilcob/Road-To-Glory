package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.quest.QuestLifecycleService;
import com.github.tilcob.game.quest.QuestRewardService;

public class RewardSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestRewardService questRewardService;

    public RewardSystem(GameEventBus eventBus, QuestRewardService questRewardService) {
        this.eventBus = eventBus;
        this.questRewardService = questRewardService;

        eventBus.subscribe(QuestRewardEvent.class, this::onQuestReward);
    }

    private void onQuestReward(QuestRewardEvent event) {
        questRewardService.claimReward(event.player(), event.questId());
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(QuestRewardEvent.class, this::onQuestReward);
    }
}
