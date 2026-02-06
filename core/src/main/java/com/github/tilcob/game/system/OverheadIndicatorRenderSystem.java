package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;

public class OverheadIndicatorRenderSystem extends IteratingSystem {
    private final AssetManager assetManager;
    private final Batch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;

    public OverheadIndicatorRenderSystem(
        AssetManager assetManager,
        Batch batch,
        Viewport viewport,
        OrthographicCamera camera
    ) {
        super(Family.all(OverheadIndicator.class, Transform.class).get());
        this.assetManager = assetManager;
        this.batch = batch;
        this.viewport = viewport;
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        super.update(deltaTime);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        OverheadIndicator indicator = OverheadIndicator.MAPPER.get(entity);
        if (!indicator.isVisible()) {
            return;
        }

        TextureRegion region = OverheadIndicatorRegistry
            .getFrame(assetManager, indicator.getCurrentType(), indicator.getTime());
        if (region == null) {
            return;
        }
        Transform transform = Transform.MAPPER.get(entity);

        float scale = indicator.getBaseScale() * indicator.getScale();
        float width = region.getRegionWidth() * Constants.UNIT_SCALE * scale;
        float height = region.getRegionHeight() * Constants.UNIT_SCALE * scale;

        Vector2 position = transform.getPosition();
        Vector2 offset = indicator.getOffset();

        float drawX = position.x + (transform.getSize().x - width) * 0.5f + offset.x;
        float drawY = position.y + offset.y + indicator.getCurrentOffsetY();

        Color indicatorColor = indicator.getColor();
        if (indicatorColor != null) {
            batch.setColor(indicatorColor);
        }
        batch.draw(region, drawX, drawY, width, height);
        batch.setColor(Color.WHITE);
    }
}
