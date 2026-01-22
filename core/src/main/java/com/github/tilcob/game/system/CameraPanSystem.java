package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.component.CameraPan;
import com.github.tilcob.game.component.CameraPanHome;

public class CameraPanSystem extends IteratingSystem {
    private final OrthographicCamera camera;

    public CameraPanSystem(OrthographicCamera camera) {
        super(Family.all(CameraPan.class).get());
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CameraPan cameraPan = CameraPan.MAPPER.get(entity);
        CameraPanHome cameraPanHome = CameraPanHome.MAPPER.get(entity);
        if (cameraPanHome == null) entity.add(new CameraPanHome(camera.position.x, camera.position.y));

        cameraPan.initStart(camera.position.x, camera.position.y);
        cameraPan.update(deltaTime);

        float a = cameraPan.alpha();
        float x = MathUtils.lerp(cameraPan.getStart().x, cameraPan.getTarget().x, a);
        float y = MathUtils.lerp(cameraPan.getStart().y, cameraPan.getTarget().y, a);
        camera.position.set(x, y, camera.position.z);
        camera.update();

        if (cameraPan.isComplete()) entity.remove(CameraPan.class);
    }
}
