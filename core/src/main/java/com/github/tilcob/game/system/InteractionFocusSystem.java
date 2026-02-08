package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.Facing;
import com.github.tilcob.game.component.Interactable;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.input.ActiveEntityReference;

public class InteractionFocusSystem extends EntitySystem {
    private static final float FRONT_BONUS = 2f;
    private static final float PRIORITY_WEIGHT = 0.25f;

    private final ActiveEntityReference activeEntityReference;
    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> interactable;

    public InteractionFocusSystem(ActiveEntityReference activeEntityReference) {
        this.activeEntityReference = activeEntityReference;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        players = engine.getEntitiesFor(Family.all(Player.class, Transform.class).get());
        interactable = engine.getEntitiesFor(Family.all(Interactable.class, Transform.class).get());
    }

    @Override
    public void update(float deltaTime) {
        Entity player = getPlayer();
        if (player == null) {
            activeEntityReference.clearFocused();
            return;
        }

        Transform playerTransform = Transform.MAPPER.get(player);
        if (playerTransform == null) {
            activeEntityReference.clearFocused();
            return;
        }

        float maxDistanceSquared = Constants.INDICATOR_SHOW_DISTANCE * Constants.INDICATOR_SHOW_DISTANCE;
        float bestScore = Float.NEGATIVE_INFINITY;
        Entity bestEntity = null;

        Vector2 playerPosition = playerTransform.getPosition();
        for (int i = 0; i < interactable.size(); i++) {
            Entity candidate = interactable.get(i);
            if (candidate == player) {
                continue;
            }
            Transform transform = Transform.MAPPER.get(candidate);
            Interactable interactable = Interactable.mapper.get(candidate);
            if (transform == null || interactable == null) {
                continue;
            }

            float distanceSquared = playerPosition.dst2(transform.getPosition());
            if (distanceSquared > maxDistanceSquared) {
                continue;
            }

            float score = -distanceSquared;
            score += interactable.getPriority() * PRIORITY_WEIGHT;
            if (isInFront(player, candidate)) {
                score += FRONT_BONUS;
            }

            if (score > bestScore) {
                bestScore = score;
                bestEntity = candidate;
            }
        }

        if (bestEntity == null) {
            activeEntityReference.clearFocused();
            return;
        }
        activeEntityReference.setFocused(bestEntity);
    }

    private boolean isInFront(Entity player, Entity candidate) {
        Facing facing = Facing.MAPPER.get(player);
        Transform playerTransform = Transform.MAPPER.get(player);
        Transform candidateTransform = Transform.MAPPER.get(candidate);
        if (facing == null || playerTransform == null || candidateTransform == null) {
            return true;
        }

        Vector2 delta = new Vector2(candidateTransform.getPosition()).sub(playerTransform.getPosition());
        return switch (facing.getDirection()) {
            case LEFT -> delta.x < 0f;
            case RIGHT -> delta.x > 0f;
            case UP -> delta.y > 0f;
            case DOWN -> delta.y < 0f;
        };
    }

    private Entity getPlayer() {
        if (players == null || players.size() == 0) {
            return null;
        }
        return players.first();
    }
}
