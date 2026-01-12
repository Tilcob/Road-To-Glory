package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.Move;
import com.github.tilcob.game.component.MoveIntent;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.component.Transform;

public class NpcPathfindingSystem extends IteratingSystem {
    private final Vector2 desired = new Vector2();

    public NpcPathfindingSystem() {
        super(Family.all(Npc.class, MoveIntent.class, Move.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        MoveIntent intent = MoveIntent.MAPPER.get(entity);
        Move move = Move.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);

        if (!intent.isActive()) {
            move.getDirection().setZero();
            return;
        }

        if (intent.isUseTarget()) {
            desired.set(intent.getTarget()).sub(transform.getPosition());
            float arrival = intent.getArrivalDistance();
            if (desired.len2() <= arrival * arrival) {
                intent.clear();
                move.getDirection().setZero();
                return;
            }
        } else {
            desired.set(intent.getDirection());
        }

        if (desired.isZero()) {
            move.getDirection().setZero();
            return;
        }

        move.getDirection().set(desired).nor();
    }
}
