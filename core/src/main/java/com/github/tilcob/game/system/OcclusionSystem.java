package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;

public class OcclusionSystem extends EntitySystem {
    private static final float DEFAULT_OCCLUSION_THRESHOLD = .6f;

    private ImmutableArray<Entity> occluders;
    private ImmutableArray<Entity> players;
    private final Rectangle tmpPlayerRect = new Rectangle();
    private final Rectangle tmpOccluderRect = new Rectangle();

    @Override
    public void addedToEngine(Engine engine) {
        occluders = engine.getEntitiesFor(
            Family.all(Transform.class, Graphic.class, MapEntity.class, Occluder.class).get()
        );
        players = engine.getEntitiesFor(Family.all(Transform.class, Player.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (players.size() == 0) return;

        Entity player = players.first();
        Transform playerTransform = Transform.MAPPER.get(player);
        toRect(playerTransform, tmpPlayerRect);

        for (int i = 0; i < occluders.size(); i++) {
            Entity occluder = occluders.get(i);
            Graphic graphic = Graphic.MAPPER.get(occluder);
            Transform transform = Transform.MAPPER.get(occluder);
            toRect(transform, tmpOccluderRect);

            if (tmpPlayerRect.overlaps(tmpOccluderRect)
                && isPlayerBehind(playerTransform, transform)) {
                graphic.getColor().a = Constants.OCCLUSION_ALPHA;
            } else {
                graphic.getColor().a = 1f;
            }
        }
    }

    private void toRect(Transform transform, Rectangle rect) {
        Vector2 position = transform.getPosition();
        Vector2 size = transform.getSize();
        Vector2 scaling = transform.getScaling();
        rect.set(position.x, position.y, size.x * scaling.x, size.y * scaling.y);
    }

    private boolean isPlayerBehind(Transform playerTransform, Transform occluderTransform) {
        float playerY = playerTransform.getPosition().y;
        float occluderY = occluderTransform.getPosition().y;
        float occluderHeight = occluderTransform.getSize().y * occluderTransform.getScaling().y;
        float threshold = occluderY + occluderHeight * DEFAULT_OCCLUSION_THRESHOLD;
        return playerY < threshold;
    }
}
