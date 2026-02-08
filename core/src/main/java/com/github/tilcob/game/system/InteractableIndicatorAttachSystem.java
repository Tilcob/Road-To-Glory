package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.indicator.IndicatorVisualDef;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;

public class InteractableIndicatorAttachSystem extends IteratingSystem {

    public InteractableIndicatorAttachSystem() {
        super(Family.all(Interactable.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        OverheadIndicator existingIndicator = OverheadIndicator.MAPPER.get(entity);
        if (existingIndicator != null) {
            if (Npc.MAPPER.get(entity) != null || NpcRole.MAPPER.get(entity) != null) {
                return;
            }
            if (existingIndicator.getCurrentType() == OverheadIndicator.OverheadIndicatorType.INTERACT_HINT) {
                return;
            }
            applyInteractHint(existingIndicator, transform);
            return;
        }

        applyInteractHint(entity, transform);
    }

    private void applyInteractHint(Entity entity, Transform transform) {
        OverheadIndicator interactHint = buildInteractHint(transform);
        entity.add(interactHint);
    }

    private void applyInteractHint(OverheadIndicator indicator, Transform transform) {
        OverheadIndicator interactHint = buildInteractHint(transform);
        indicator.setCurrentType(interactHint.getCurrentType());
        indicator.setDesiredType(null);
        indicator.getOffset().set(interactHint.getOffset());
        indicator.setBaseScale(interactHint.getBaseScale());
        indicator.setColor(interactHint.getColor());
        indicator.setAllowBob(interactHint.getAllowBob());
        indicator.setAllowPulse(interactHint.getAllowPulse());
    }

    private OverheadIndicator buildInteractHint(Transform transform) {
        float offsetY = Constants.DEFAULT_INDICATOR_OFFSET_Y;
        if (transform != null) {
            offsetY = transform.getSize().y
                + Constants.DEFAULT_INDICATOR_OFFSET_Y
                - 8f * Constants.UNIT_SCALE;
        }
        IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(
            OverheadIndicator.OverheadIndicatorType.INTERACT_HINT
        );
        float baseScale = visualDef == null ? 1f : visualDef.defaultScale();

        OverheadIndicator interactHint = new OverheadIndicator(
            OverheadIndicator.OverheadIndicatorType.INTERACT_HINT,
            new Vector2(0, offsetY),
            baseScale,
            Color.WHITE.cpy(),
            true
        );

        interactHint.setAllowBob(true);
        interactHint.setAllowPulse(true);
        return interactHint;
    }
}
