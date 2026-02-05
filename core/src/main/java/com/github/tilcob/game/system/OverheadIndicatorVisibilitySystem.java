package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;

public class OverheadIndicatorVisibilitySystem extends IteratingSystem {
    private final float maxDistance;
    private final float maxDistanceSquared;
    private ImmutableArray<Entity> players;

    public OverheadIndicatorVisibilitySystem() {
        this(Constants.INDICATOR_MAX_DISTANCE);
    }

    public OverheadIndicatorVisibilitySystem(float maxDistance) {
        super(Family.all(OverheadIndicator.class, Transform.class).get());
        this.maxDistance = maxDistance;
        this.maxDistanceSquared = maxDistance * maxDistance;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        players = engine.getEntitiesFor(Family.all(Player.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        OverheadIndicator indicator = OverheadIndicator.MAPPER.get(entity);
        if (!indicator.isVisible()) {
            return;
        }

        Entity player = getPlayer();
        if (player == null) {
            return;
        }

        Transform playerTransform = Transform.MAPPER.get(player);
        Transform transform = Transform.MAPPER.get(entity);
        if (playerTransform == null || transform == null) {
            return;
        }

        float distanceSquared = playerTransform.getPosition().dst2(transform.getPosition());
        if (distanceSquared > maxDistanceSquared) {
            indicator.setVisible(false);
        }
    }

    private Entity getPlayer() {
        if (players == null || players.size() == 0) {
            return null;
        }
        return players.first();
    }

    public float getMaxDistance() {
        return maxDistance;
    }
}
