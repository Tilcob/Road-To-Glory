package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.GameOverEvent;
import com.github.tilcob.game.npc.NpcType;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.ui.model.GameViewModel;

import java.util.Locale;

public class DamageSystem extends IteratingSystem {
    private final GameViewModel viewModel;
    private final QuestManager questManager;
    private final GameEventBus eventBus;

    public DamageSystem(GameViewModel viewModel, QuestManager questManager, GameEventBus eventBus) {
        super(Family.all(Damaged.class).get());
        this.viewModel = viewModel;
        this.questManager = questManager;
        this.eventBus = eventBus;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Damaged damaged = Damaged.MAPPER.get(entity);
        entity.remove(Damaged.class);

        Life life = Life.MAPPER.get(entity);
        Protection protection = Protection.MAPPER.get(entity);
        float dealtDamage = damaged.getDamage();
        if (protection != null) {
            dealtDamage *= (1f - Math.max(0f, Math.min(.95f, protection.getProtection() / 100f)));
        }

        if (life != null) {
            dealtDamage = Math.min(dealtDamage, life.getLife());
            life.addLife(-dealtDamage);
            if (life.getLife() <= 0 && Player.MAPPER.get(entity) == null) {
                Npc npc = Npc.MAPPER.get(entity);
                if (npc != null && npc.getType() == NpcType.ENEMY) {
                    Entity player = resolvePlayer();
                    if (player != null) {
                        incrementCounter(player, killCounterKey(npc.getName()), 1);
                        questManager.signal(resolvePlayer(), "kill", npc.getName(), 1);
                    }
                }
                getEngine().removeEntity(entity);
                return;
            } else if (life.getLife() <= 0 && Player.MAPPER.get(entity) != null) {
                eventBus.fire(new GameOverEvent(entity));
            }
        }

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null && dealtDamage > 0f) {
            float x = transform.getPosition().x + transform.getSize().x * .5f;
            float y = transform.getPosition().y;
            viewModel.playerDamage((int) dealtDamage, x, y);
        }
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
}
