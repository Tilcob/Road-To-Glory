package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.IndicatorCommandLifetime;
import com.github.tilcob.game.component.OverheadIndicator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndicatorCommandLifetimeSystemTest {

    @Test
    void expiresAndRestoresIndicatorState() {
        Engine engine = new Engine();
        engine.addSystem(new IndicatorCommandLifetimeSystem());

        Entity npc = new Entity();
        OverheadIndicator indicator = new OverheadIndicator(
            OverheadIndicator.OverheadIndicatorType.TALKING,
            new Vector2(),
            1f,
            Color.WHITE.cpy(),
            true
        );
        npc.add(indicator);
        npc.add(new IndicatorCommandLifetime(0.3f, OverheadIndicator.OverheadIndicatorType.INFO, false));
        engine.addEntity(npc);

        engine.update(0.2f);
        assertNotNull(IndicatorCommandLifetime.MAPPER.get(npc));

        engine.update(0.2f);
        assertNull(IndicatorCommandLifetime.MAPPER.get(npc));
        assertEquals(OverheadIndicator.OverheadIndicatorType.INFO, indicator.getIndicatorId());
        assertFalse(indicator.isVisible());
    }
}
