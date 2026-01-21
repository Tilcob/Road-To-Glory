package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.CameraPan;
import com.github.tilcob.game.component.CameraFollow;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;

public class CameraSystem extends IteratingSystem {
    private final Camera camera;
    private final Vector2 targetPosition;
    private float mapWidth;
    private float mapHeight;
    private final float smoothingFactor;

    public CameraSystem(Camera camera) {
        super(Family.all(CameraFollow.class, Transform.class).get());
        this.camera = camera;
        this.targetPosition = new Vector2();
        this.smoothingFactor = 4f;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        CameraPan cameraPan = CameraPan.MAPPER.get(entity);
        if (cameraPan != null) {
            cameraPan.update(deltaTime);
            calcTargetPosition(cameraPan.getTarget());
            if (cameraPan.isComplete()) {
                entity.remove(CameraPan.class);
            }
        } else {
            calcTargetPosition(transform.getPosition());
        }

        float progress = smoothingFactor * deltaTime;
        float smoothedX = MathUtils.lerp(camera.position.x, targetPosition.x, progress);
        float smoothedY = MathUtils.lerp(camera.position.y, targetPosition.y, progress);
        camera.position.set(smoothedX, smoothedY, camera.position.z);
    }

    private void calcTargetPosition(Vector2 entityPosition) {
        float targetX = entityPosition.x;
        float targetY = entityPosition.y + Constants.CAMERA_OFFSET_Y;
        float cameraHalfWidth = camera.viewportWidth * .5f;
        float cameraHalfHeight = camera.viewportHeight * .5f;

        if (mapWidth > cameraHalfWidth) {
            float min = Math.min(cameraHalfWidth, mapWidth - cameraHalfWidth);
            float max = Math.max(cameraHalfWidth, mapWidth - cameraHalfWidth);
            targetX = MathUtils.clamp(targetX, min, max);
        }

        if (mapHeight > cameraHalfHeight) {
            float min = Math.min(cameraHalfHeight, mapHeight - cameraHalfHeight);
            float max = Math.max(cameraHalfHeight, mapHeight - cameraHalfHeight);
            targetY = MathUtils.clamp(targetY, min, max);
        }

        this.targetPosition.set(targetX, targetY);
    }

    public void setMap(TiledMap tiledMap) {
        int width = tiledMap.getProperties().get(Constants.MAP_WIDTH, 0, Integer.class);
        int height = tiledMap.getProperties().get(Constants.MAP_HEIGHT, 0, Integer.class);
        int tileWidth = tiledMap.getProperties().get(Constants.TILE_WIDTH, 0, Integer.class);
        int tileHeight = tiledMap.getProperties().get(Constants.TILE_HEIGHT, 0, Integer.class);

        this.mapWidth = width * tileWidth * Constants.UNIT_SCALE;
        this.mapHeight = height * tileHeight * Constants.UNIT_SCALE;

        Entity camEntity = getEntities().first();
        if (camEntity == null) return;

        processEntity(camEntity, 0);
    }
}
