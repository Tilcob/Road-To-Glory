package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.entity.EntityLookup;
import com.github.tilcob.game.event.PlayIndicatorEvent;
import com.github.tilcob.game.flow.CommandCall;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowAction;
import com.github.tilcob.game.flow.FlowContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CutsceneCommandModuleTest {
    @Test
    void playIndicatorEmitsEventForNpcWithoutRole() {
        Entity player = new Entity();
        Entity npcWithoutRole = new Entity();

        EntityLookup lookup = new EntityLookup() {
            @Override
            public Entity find(String name) {
                return "npc_no_role".equals(name) ? npcWithoutRole : null;
            }

            @Override
            public Entity getPlayer() {
                return player;
            }
        };

        CommandRegistry registry = new CommandRegistry();
        new CutsceneCommandModule(() -> lookup).register(registry);

        List<FlowAction> actions = registry.dispatch(
            CommandCall.simple("play_indicator", List.of("npc_no_role", "INFO")),
            new FlowContext(player)
        );

        FlowAction.EmitEvent emitEvent = assertInstanceOf(FlowAction.EmitEvent.class, actions.get(0));
        PlayIndicatorEvent event = assertInstanceOf(PlayIndicatorEvent.class, emitEvent.event());

        assertSame(player, event.player());
        assertSame(npcWithoutRole, event.target());
        assertEquals(OverheadIndicator.OverheadIndicatorType.INFO, event.indicatorType());
    }
}
