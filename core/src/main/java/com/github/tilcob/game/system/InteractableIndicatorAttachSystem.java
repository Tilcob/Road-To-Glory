package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.Interactable;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.indicator.IndicatorVisualDef;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;

public class InteractableIndicatorAttachSystem extends IteratingSystem {

    public InteractableIndicatorAttachSystem() {
        super(Family.all(Interactable.class, Transform.class).exclude(OverheadIndicator.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        float offestY = Constants.DEFAULT_INDICATOR_OFFSET_Y;
        if (transform != null) {
            offestY = Constants.DEFAULT_INDICATOR_OFFSET_Y * 3;
        }
        IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(
            OverheadIndicator.OverheadIndicatorType.INTERACT_HINT
        );
        float baseScale = visualDef == null ? 1f : visualDef.defaultScale();

        OverheadIndicator interactHint = new OverheadIndicator(
            OverheadIndicator.OverheadIndicatorType.INTERACT_HINT,
            new Vector2(0, offestY),
            baseScale,
            Color.WHITE.cpy(),
            true
        );

        interactHint.setAllowBob(true);
        interactHint.setAllowPulse(true);
        entity.add(interactHint);
    }
}
