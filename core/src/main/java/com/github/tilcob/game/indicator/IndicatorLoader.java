package com.github.tilcob.game.indicator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.input.InputBindings;

import static com.github.tilcob.game.component.OverheadIndicator.OverheadIndicatorType.*;

public class IndicatorLoader {

    public static void load(InputBindings inputBindings) {
        OverheadIndicatorRegistry.clear();

        register(QUEST_AVAILABLE, "quest_available",
            new IndicatorVisualDef(1f, 0f, 0f, 0.15f, 2.5f, 0.08f, 3.0f));

        register(QUEST_TURNING, "quest_available",
            new IndicatorVisualDef(1f, 0f, 0f, 0.20f, 2.8f, 0.10f, 3.2f));

        register(INFO, "talking",
            new IndicatorVisualDef(2f, 0f, 0f, 0.10f, 2.3f, 0.06f, 2.8f));

        register(MERCHANT, "talking",
            new IndicatorVisualDef(2f, 0f, 0f, 0.10f, 2.3f, 0.06f, 2.8f));

        register(TALK_AVAILABLE, "talking",
            new IndicatorVisualDef(2f, 0f, 0f, 0.10f, 2.3f, 0.06f, 2.8f));

        register(TALK_IN_RANGE, "talking",
            new IndicatorVisualDef(2f, 0f, 0f, 0.12f, 2.5f, 0.07f, 2.8f));

        register(TALK_BUSY, "talking",
            new IndicatorVisualDef(2f, 0f, 0f, 0.18f, 2.8f, 0.10f, 3.2f));

        register(TALK_CHOICE, "talking",
            new IndicatorVisualDef(2f, 0f, 0f, 0.20f, 3.0f, 0.10f, 3.2f));

        register(INTERACT_HINT, "key_" + inputBindings.getPrimaryBindingLabel(Command.INTERACT).toLowerCase(),
            new IndicatorVisualDef(.3f, 0f, 0f, 0.08f, 2.6f, 0.05f, 3.0f));

        register(DANGER, "angry",
            new IndicatorVisualDef(2f, 0f, 0f, 0.20f, 3.0f, 0.12f, 3.4f));

        register(ANGRY, "angry",
            new IndicatorVisualDef(2f, 0f, 0f, 0.20f, 3.0f, 0.12f, 3.4f));

        register(TALKING, "talking",
            new IndicatorVisualDef(10f, 0f, 0f, 0f, 2.5f, 0f, 3.0f));

        register(HAPPY, "happy",
            new IndicatorVisualDef(2f, 0f, 0f, 0.20f, 3.0f, 0.12f, 3.4f));

        register(SAD, "sad",
            new IndicatorVisualDef(2f, 0f, 0f, 0.20f, 3.0f, 0.12f, 3.4f));

    }

    private static void register(
        OverheadIndicator.OverheadIndicatorType type,
        String regionKey, float frameDuration, Animation.PlayMode playMode,
        IndicatorVisualDef indicatorVisualDef) {

        OverheadIndicatorRegistry.register(
            type,
            AtlasAsset.INDICATORS,
            regionKey,
            frameDuration,
            playMode,
            indicatorVisualDef
        );
    }

    private static void register(OverheadIndicator.OverheadIndicatorType type,
                                 String regionKey, IndicatorVisualDef indicatorVisualDef) {

        OverheadIndicatorRegistry.register(
            type,
            AtlasAsset.INDICATORS,
            regionKey,
            Constants.FRAME_DURATION,
            Animation.PlayMode.LOOP,
            indicatorVisualDef
        );
    }
}
