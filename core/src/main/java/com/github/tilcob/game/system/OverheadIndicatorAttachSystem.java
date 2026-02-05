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

public class OverheadIndicatorAttachSystem extends IteratingSystem {
    public OverheadIndicatorAttachSystem() {
        super(Family.all(Transform.class).one(Interactable.class, Chest.class).exclude(OverheadIndicator.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        NpcRole npcRole = NpcRole.MAPPER.get(entity);
        OverheadIndicator.OverheadIndicatorType defaultType = defaultTypeForEntity(entity, npcRole);
        OverheadIndicator indicator = createDefaultIndicator(entity, defaultType);
        entity.add(indicator);

        if (OverheadIndicatorAnimation.MAPPER.get(entity) == null) {
            entity.add(new OverheadIndicatorAnimation(0f, 0f, 0f, 0f, 1f));
        }
    }

    private boolean isIndicatorRelevant(NpcRole npcRole) {
        return npcRole != null && npcRole.getRole() != null;
    }

    private OverheadIndicator.OverheadIndicatorType defaultTypeForEntity(Entity entity, NpcRole npcRole) {
        if (entity != null && Npc.MAPPER.has(entity) && isIndicatorRelevant(npcRole)) {
            return defaultTypeForRole(npcRole.getRole());
        }
        return OverheadIndicator.OverheadIndicatorType.INTERACT_HINT;
    }

    private OverheadIndicator.OverheadIndicatorType defaultTypeForRole(NpcRole.Role role) {
        if (role == NpcRole.Role.QUEST_GIVER) {
            return OverheadIndicator.OverheadIndicatorType.QUEST_AVAILABLE;
        }
        if (role == NpcRole.Role.DANGER) {
            return OverheadIndicator.OverheadIndicatorType.DANGER;
        }
        return OverheadIndicator.OverheadIndicatorType.INFO;
    }

    private OverheadIndicator createDefaultIndicator(
        Entity entity,
        OverheadIndicator.OverheadIndicatorType type
    ) {
        IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(type);
        float offsetX = visualDef == null ? 0f : visualDef.defaultOffsetX();
        float offsetY = resolveDefaultOffsetY(entity, visualDef);
        float scale = visualDef == null ? 1f : visualDef.defaultScale();

        OverheadIndicator indicator = new OverheadIndicator(
            type,
            new Vector2(offsetX, offsetY),
            scale,
            Color.WHITE.cpy(),
            true
        );
        indicator.setAllowBob(true);
        indicator.setAllowPulse(true);
        return indicator;
    }

    private float resolveDefaultOffsetY(Entity entity, IndicatorVisualDef visualDef) {
        Transform transform = Transform.MAPPER.get(entity);
        float offsetY = Constants.DEFAULT_INDICATOR_OFFSET_Y;
        if (transform != null) {
            offsetY = transform.getSize().y + Constants.DEFAULT_INDICATOR_OFFSET_Y - 8f * Constants.UNIT_SCALE;
        }
        if (visualDef != null) {
            offsetY += visualDef.defaultOffsetY();
        }
        return offsetY;
    }
}
