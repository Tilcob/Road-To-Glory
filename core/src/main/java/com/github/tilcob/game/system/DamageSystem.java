package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Damaged;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.ui.model.GameViewModel;

public class DamageSystem extends IteratingSystem {
    private final GameViewModel viewModel;

    public DamageSystem(GameViewModel viewModel) {
        super(Family.all(Damaged.class).get());
        this.viewModel = viewModel;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Damaged damaged = Damaged.MAPPER.get(entity);
        entity.remove(Damaged.class);

        Life life = Life.MAPPER.get(entity);
        if (life != null) life.addLife(-damaged.getDamage());

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null) {
            // we should check that the damage source is the player, but
            // in this tutorial game it is in the only possibility so we can skip it

            float x = transform.getPosition().x + transform.getSize().x * .5f;
            float y = transform.getPosition().y;
            viewModel.playerDamage((int) damaged.getDamage(), x, y);
        }
    }
}
