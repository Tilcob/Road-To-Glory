package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.component.Transform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverheadIndicatorStateMachineSystemTest {

        @Test
        void keepsNonInteractIndicatorsVisibleEvenOutOfRange() {
            Engine engine = new Engine();
            engine.addSystem(new OverheadIndicatorStateMachineSystem(null, null, 1.5f, 2f));

            Entity player = new Entity();
            player.add(new Player());
            player.add(new Transform(new Vector2(0f, 0f), 0, new Vector2(1f, 1f), new Vector2(1f, 1f), 0f));
            engine.addEntity(player);

            Entity npc = new Entity();
            OverheadIndicator indicator = new OverheadIndicator(
                OverheadIndicator.OverheadIndicatorType.QUEST_AVAILABLE,
                new Vector2(),
                1f,
                Color.WHITE.cpy(),
                false
            );
            npc.add(indicator);
            npc.add(new Transform(new Vector2(10f, 0f), 0, new Vector2(1f, 1f), new Vector2(1f, 1f), 0f));
            engine.addEntity(npc);

            engine.update(0.5f);

            assertTrue(indicator.isVisible());
        }

        @Test
        void appliesDistanceVisibilityOnlyToInteractHint() {
            Engine engine = new Engine();
            engine.addSystem(new OverheadIndicatorStateMachineSystem(null, null, 1.5f, 2f));

            Entity player = new Entity();
            player.add(new Player());
            player.add(new Transform(new Vector2(0f, 0f), 0, new Vector2(1f, 1f), new Vector2(1f, 1f), 0f));
            engine.addEntity(player);

            Entity npc = new Entity();
            OverheadIndicator indicator = new OverheadIndicator(
                OverheadIndicator.OverheadIndicatorType.INTERACT_HINT,
                new Vector2(),
                1f,
                Color.WHITE.cpy(),
                true
            );
            npc.add(indicator);
            npc.add(new Transform(new Vector2(3f, 0f), 0, new Vector2(1f, 1f), new Vector2(1f, 1f), 0f));
            engine.addEntity(npc);

            engine.update(0.5f);

            assertFalse(indicator.isVisible());
        }
    }
