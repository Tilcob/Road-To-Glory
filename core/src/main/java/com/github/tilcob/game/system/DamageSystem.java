package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Damaged;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.quest.KillEvent;
import com.github.tilcob.game.npc.NpcType;
import com.github.tilcob.game.ui.model.GameViewModel;

public class DamageSystem extends IteratingSystem {
    private final GameViewModel viewModel;
    private final GameEventBus eventBus;

    public DamageSystem(GameViewModel viewModel, GameEventBus eventBus) {
        super(Family.all(Damaged.class).get());
        this.viewModel = viewModel;
        this.eventBus = eventBus;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Damaged damaged = Damaged.MAPPER.get(entity);
        entity.remove(Damaged.class);

        Life life = Life.MAPPER.get(entity);
        float dealtDamage = damaged.getDamage();
        if (life != null) {
            dealtDamage = Math.min(damaged.getDamage(), life.getLife());
            life.addLife(-damaged.getDamage());
            if (life.getLife() <= 0 && Player.MAPPER.get(entity) == null) {
                Npc npc = Npc.MAPPER.get(entity);
                if (npc != null && npc.getType() == NpcType.ENEMY) {
                    eventBus.fire(new KillEvent(npc.getName(), 1));
                }
                getEngine().removeEntity(entity);
                return;
            }
        }

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null && dealtDamage > 0f) {
            float x = transform.getPosition().x + transform.getSize().x * .5f;
            float y = transform.getPosition().y;
            viewModel.playerDamage((int) dealtDamage, x, y);
        }
    }
}
