package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.Attack;
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
        Attack attack = Attack.MAPPER.get(entity);
        if (attack != null && attack.isInWindup()) {
            return;
        }

        Facing facing = Facing.MAPPER.get(entity);
        if (Math.abs(direction.x) >= Math.abs(direction.y)) {
            facing.setDirection(direction.x >= 0
                ? Facing.FacingDirection.RIGHT
                : Facing.FacingDirection.LEFT);
        } else {
            facing.setDirection(direction.y >= 0
                ? Facing.FacingDirection.UP
                : Facing.FacingDirection.DOWN);
        }
    }
}
