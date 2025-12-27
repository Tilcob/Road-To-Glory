package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.Facing;
import com.github.tilcob.game.component.Move;

public class FacingSystem extends IteratingSystem {

    public FacingSystem() {
        super(Family.all(Facing.class, Move.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = Move.MAPPER.get(entity);
        Vector2 direction = move.getDirection();
        if (direction.isZero()) return;

        Facing facing = Facing.MAPPER.get(entity);
        if (direction.y > 0) {
            facing.setDirection(Facing.FacingDirection.UP);
        } else if (direction.y < 0) {
            facing.setDirection(Facing.FacingDirection.DOWN);
        } else if (direction.x > 0) {
            facing.setDirection(Facing.FacingDirection.RIGHT);
        } else {
            facing.setDirection(Facing.FacingDirection.LEFT);
        }
    }
}
