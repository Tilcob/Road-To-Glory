package com.github.tilcob.game.indicator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.component.OverheadIndicator.OverheadIndicatorType;
import com.github.tilcob.game.config.Constants;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for overhead indicator regions.
 *
 * <p>Region names should follow the schema {@code "indicators/<indicator_key>"} to keep art assets
 * consistent (for example {@code "indicators/quest_available"}).</p>
 */
public final class OverheadIndicatorRegistry {
    private static final Map<OverheadIndicatorType, IndicatorAnimationDef> REGISTRY =
        new EnumMap<>(OverheadIndicatorType.class);
    private static final Map<OverheadIndicatorType, Animation<TextureRegion>> ANIMATION_CACHE =
        new EnumMap<>(OverheadIndicatorType.class);

    private OverheadIndicatorRegistry() {
    }

    public static void register(OverheadIndicatorType type, AtlasAsset atlasAsset, String regionKey) {
        register(type, atlasAsset, regionKey, Constants.FRAME_DURATION, Animation.PlayMode.LOOP);
    }

    public static void register(
        OverheadIndicatorType type,
        AtlasAsset atlasAsset,
        String regionKey,
        float frameDuration,
        Animation.PlayMode playMode
    ) {
        if (type == null) throw new IllegalArgumentException("Overhead indicator type must not be null.");
        if (atlasAsset == null) throw new IllegalArgumentException("Atlas asset must not be null.");
        if (regionKey == null || regionKey.isBlank()) throw new IllegalArgumentException("Region key must be non-empty.");
        if (frameDuration <= 0f) throw new IllegalArgumentException("Frame duration must be greater than 0.");
        if (playMode == null) throw new IllegalArgumentException("Play mode must not be null.");
        if (REGISTRY.containsKey(type)) {
            throw new IllegalArgumentException("Overhead indicator already registered: " + type);
        }
        REGISTRY.put(type, new IndicatorAnimationDef(atlasAsset, regionKey, frameDuration, playMode));
    }

    public static TextureRegion getRegion(AssetManager assetManager, OverheadIndicatorType type) {
        return getFrame(assetManager, type, 0f);
    }

    public static TextureRegion getFrame(AssetManager assetManager, OverheadIndicatorType type, float stateTime) {
        if (assetManager == null) throw new IllegalArgumentException("Asset manager must not be null.");
        if (type == null) throw new IllegalArgumentException("Overhead indicator type must not be null.");

        IndicatorAnimationDef definition = REGISTRY.get(type);
        if (definition == null) {
            throw new IllegalArgumentException("No region registered for overhead indicator: " + type);
        }
        Animation<TextureRegion> animation = ANIMATION_CACHE
            .computeIfAbsent(type, key -> createAnimation(assetManager, definition));
        return animation.getKeyFrame(Math.max(stateTime, 0f));
    }

    private static Animation<TextureRegion> createAnimation(AssetManager assetManager, IndicatorAnimationDef definition) {
        TextureAtlas atlas = assetManager.get(definition.atlasAsset());
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(definition.regionKey());
        if (regions.isEmpty()) {
            TextureAtlas.AtlasRegion region = atlas.findRegion(definition.regionKey());
            if (region == null) {
                throw new GdxRuntimeException("No atlas region found for overhead indicator: " + definition.regionKey());
            }
            regions = new Array<>();
            regions.add(region);
        }

        Animation<TextureRegion> animation = new Animation<>(definition.frameDuration(), regions);
        animation.setPlayMode(definition.playMode());
        return animation;
    }

    public static void clear() {
        REGISTRY.clear();
        ANIMATION_CACHE.clear();
    }

    public record IndicatorAnimationDef(
        AtlasAsset atlasAsset,
        String regionKey,
        float frameDuration,
        Animation.PlayMode playMode
    ) {
    }
}
