package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.ui.model.GameViewModel;

public class LifeSystem extends IteratingSystem implements EntityListener {
    private final GameViewModel viewModel;

    public LifeSystem(GameViewModel viewModel) {
        super(Family.all(Life.class).get());
        this.viewModel = viewModel;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(Family.all(Life.class, Player.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    @Override
    public void entityAdded(Entity entity) {
        Life life = Life.MAPPER.get(entity);
        viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Life life = Life.MAPPER.get(entity);
        if (life.getLife() == life.getMaxLife()) return;

        life.addLife(life.getLifePerSec() * deltaTime);
        if (Player.MAPPER.get(entity) != null) {
            viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
        }
    }
}
