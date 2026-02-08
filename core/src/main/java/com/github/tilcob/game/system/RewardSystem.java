package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.npc.NpcType;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.quest.QuestRewardService;

import java.util.Locale;

public class RewardSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestRewardService questRewardService;
    private final QuestManager questManager;

    public RewardSystem(GameEventBus eventBus, QuestManager questManager,
                        QuestRewardService questRewardService) {
        this.eventBus = eventBus;
        this.questRewardService = questRewardService;
        this.questManager = questManager;

        eventBus.subscribe(QuestRewardEvent.class, this::onQuestReward);
        eventBus.subscribe(CommandEvent.class, this::onCommand);
        eventBus.subscribe(EntityDeathEvent.class, this::onEntityDeath);
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

    private void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.entity();
        Npc npc = Npc.MAPPER.get(entity);
        if (npc == null || npc.getType() != NpcType.ENEMY) return;

        Entity player = resolvePlayer();
        if (player == null) return;

        incrementCounter(player, killCounterKey(npc.getName()), 1);
        questManager.signal(player, "kill", npc.getName(), 1);

        String npcType = npc.getType() == null ? "unknown" : npc.getType().name().toLowerCase(Locale.ROOT);
        ExpMultiplier expComp = ExpMultiplier.MAPPER.get(entity);
        float expMultiplier = expComp != null ? expComp.getExpMultiplier() : 1f;
        eventBus.fire(new ExpGainRequestEvent(player, "combat", npcType, expMultiplier, Constants.BASE_EXP));
    }

    private Entity resolvePlayer() {
        ImmutableArray<Entity> players = getEngine().getEntitiesFor(Family.all(Player.class).get());
        if (players == null || players.size() == 0) return null;
        return players.first();
    }

    private void incrementCounter(Entity player, String key, int value) {
        Counters counters = Counters.MAPPER.get(player);
        if (counters == null) {
            counters = new Counters();
            player.add(counters);
        }
        counters.increment(key, value);
    }

    private String killCounterKey(String npcName) {
        if (npcName == null) return "kill:unknown";
        return "kill:" + npcName.toLowerCase(Locale.ROOT);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(QuestRewardEvent.class, this::onQuestReward);
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
        eventBus.unsubscribe(EntityDeathEvent.class, this::onEntityDeath);
    }
}
