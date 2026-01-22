package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.component.ScreenFade;

public class ScreenFadeSystem extends IteratingSystem implements Disposable {
    private final Batch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final Texture pixelTexture;
    private float maxAlpha;

    public ScreenFadeSystem(Batch batch, Viewport viewport, OrthographicCamera camera) {
        super(Family.all(ScreenFade.class).get());
        this.batch = batch;
        this.viewport = viewport;
        this.camera = camera;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.pixelTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void update(float deltaTime) {
        maxAlpha = 0f;
        super.update(deltaTime);
        if (maxAlpha <= 0f) {
            return;
        }
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0f, 0f, 0f, maxAlpha);
        float width = camera.viewportWidth;
        float height = camera.viewportHeight;
        float x = camera.position.x - width * 0.5f;
        float y = camera.position.y - height * 0.5f;
        batch.draw(pixelTexture, x, y, width, height);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ScreenFade fade = ScreenFade.MAPPER.get(entity);
        if (fade == null) {
            return;
        }
        fade.update(deltaTime);
        maxAlpha = Math.max(maxAlpha, fade.getCurrentAlpha());
        if (!fade.isActive() && fade.getTargetAlpha() <= 0f) {
            entity.remove(ScreenFade.class);
        }
    }

    @Override
    public void dispose() {
        pixelTexture.dispose();
    }
}
