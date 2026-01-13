package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.component.Graphic;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RenderSystem extends SortedIteratingSystem implements Disposable {
    private final OrthogonalTiledMapRenderer mapRenderer; // TiledMapRenderer is the parent type
    private final Viewport viewport;
    private  final Batch batch;
    private final OrthographicCamera camera;
    private final List<MapLayer> foreGroundLayers = new ArrayList<>();
    private final List<MapLayer> backgroundLayers = new ArrayList<>();

    public RenderSystem(Batch batch, Viewport viewport, OrthographicCamera camera) {
        super(
            Family.all(Transform.class, Graphic.class).get(),
            Comparator.comparing(Transform.MAPPER::get)
        );
        this.batch = batch;
        this.viewport = viewport;
        this.camera = camera;
        this.mapRenderer = new OrthogonalTiledMapRenderer(null, Constants.UNIT_SCALE, this.batch);
    }

    @Override
    public void update(float deltaTime) {
        AnimatedTiledMapTile.updateAnimationBaseTime();
        viewport.apply();

        batch.begin();
        batch.setColor(Color.WHITE);
        mapRenderer.setView(camera);
        backgroundLayers.forEach(mapRenderer::renderMapLayer);
        forceSort();
        super.update(deltaTime);
        batch.setColor(Color.WHITE);
        foreGroundLayers.forEach(mapRenderer::renderMapLayer);
        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float v) {
        Transform transform = Transform.MAPPER.get(entity);
        Graphic graphic = Graphic.MAPPER.get(entity);

        if (graphic.getRegion() == null) return;

        Vector2 position = transform.getPosition();
        Vector2 scaling = transform.getScaling();
        Vector2 size = transform.getSize();

        batch.setColor(graphic.getColor());
        batch.draw(
            graphic.getRegion(),
            position.x - size.x * (1 - scaling.x) * .5f,
            position.y - size.y * (1 - scaling.y) * .5f,
            size.x * .5f, size.y * .5f,
            size.x, size.y,
            scaling.x, scaling.y,
            transform.getRotationDegrees()
        );
        batch.setColor(Color.WHITE);
    }

    public void setMap(TiledMap map) {
        mapRenderer.setMap(map);

        foreGroundLayers.clear();
        backgroundLayers.clear();
        if (map == null) return;
        List<MapLayer> currentLayers = backgroundLayers;

        for (MapLayer layer : map.getLayers()) {
            if (Constants.OBJECT_LAYER.equals(layer.getName())) {
                currentLayers = foreGroundLayers;
                continue;
            }
            addRenderableLayer(layer, currentLayers);
        }
    }

    private void addRenderableLayer(MapLayer layer, List<MapLayer> target) {
        if (layer instanceof MapGroupLayer groupLayer) {
            for (MapLayer child : groupLayer.getLayers()) {
                addRenderableLayer(child, target);
            }
            return;
        }
        if (layer.getClass().equals(MapLayer.class)) return;
        if (Constants.OBJECT_LAYER.equals(layer.getName()) || !layer.isVisible()) return;
        target.add(layer);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
    }
}
