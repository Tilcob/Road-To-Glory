package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.RewardDialogState;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.DialogAdvanceEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.quest.QuestRewardService;

public class RewardSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestRewardService questRewardService;

    public RewardSystem(GameEventBus eventBus, QuestRewardService questRewardService) {
        this.eventBus = eventBus;
        this.questRewardService = questRewardService;

        eventBus.subscribe(QuestRewardEvent.class, this::onQuestReward);
        eventBus.subscribe(CommandEvent.class, this::onCommand);
    }

    private void onQuestReward(QuestRewardEvent event) {
        questRewardService.claimReward(event.player(), event.questId());
    }

    private void onCommand(CommandEvent event) {
        if (event.isHandled()) return;
        if (event.getCommand() != Command.INTERACT) return;
        if (RewardDialogState.MAPPER.get(event.getPlayer()) == null) return;
        event.setHandled(true);
        eventBus.fire(new DialogAdvanceEvent(event.getPlayer()));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(QuestRewardEvent.class, this::onQuestReward);
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
