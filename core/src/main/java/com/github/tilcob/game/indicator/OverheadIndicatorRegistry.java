package com.github.tilcob.game.indicator;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.component.OverheadIndicator.OverheadIndicatorType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for overhead indicator regions.
 *
 * <p>Region names should follow the schema {@code "indicators/<indicator_key>"} to keep art assets
 * consistent (for example {@code "indicators/quest_available"}).</p>
 */
public final class OverheadIndicatorRegistry {
    private static final Map<OverheadIndicatorType, IndicatorRegion> REGISTRY =
        new EnumMap<>(OverheadIndicatorType.class);

    private OverheadIndicatorRegistry() {
    }

    public static void register(OverheadIndicatorType type, AtlasAsset atlasAsset, String regionKey) {
        if (type == null) throw new IllegalArgumentException("Overhead indicator type must not be null.");
        if (atlasAsset == null) throw new IllegalArgumentException("Atlas asset must not be null.");
        if (regionKey == null || regionKey.isBlank()) throw new IllegalArgumentException("Region key must be non-empty.");
        if (REGISTRY.containsKey(type)) {
            throw new IllegalArgumentException("Overhead indicator already registered: " + type);
        }
        REGISTRY.put(type, new IndicatorRegion(atlasAsset, regionKey));
    }

    public static TextureRegion getRegion(AssetManager assetManager, OverheadIndicatorType type) {
        if (assetManager == null) throw new IllegalArgumentException("Asset manager must not be null.");
        if (type == null) throw new IllegalArgumentException("Overhead indicator type must not be null.");

        IndicatorRegion region = REGISTRY.get(type);
        if (region == null) {
            throw new IllegalArgumentException("No region registered for overhead indicator: " + type);
        }

        TextureAtlas atlas = assetManager.get(region.atlasAsset());
        TextureRegion textureRegion = atlas.findRegion(region.regionKey());
        if (textureRegion == null) {
            throw new GdxRuntimeException("No atlas region found for overhead indicator: " + region.regionKey());
        }
        return textureRegion;
    }

    public static void clear() {
        REGISTRY.clear();
    }

    public record IndicatorRegion(AtlasAsset atlasAsset, String regionKey) {
    }
}
