package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.DeathHandled;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.event.EntityDeathEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.GameOverEvent;

public class DeathSystem extends IteratingSystem {
    private final GameEventBus eventBus;

    public DeathSystem(GameEventBus eventBus) {
        super(Family.all(Life.class).get());
        this.eventBus = eventBus;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Life life = Life.MAPPER.get(entity);
        DeathHandled handled = DeathHandled.MAPPER.get(entity);

        if (life.getLife() > 0f) {
            if (handled != null) entity.remove(DeathHandled.class);
            return;
        }

        if (handled != null) return;

        if (Player.MAPPER.get(entity) != null) {
            eventBus.fire(new GameOverEvent(entity));
            entity.add(new DeathHandled());
            return;
        }

        eventBus.fire(new EntityDeathEvent(entity));
        entity.add(new DeathHandled());
        getEngine().removeEntity(entity);
    }
}
