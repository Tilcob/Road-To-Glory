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

class DialogCommandModuleTest {

    @Test
    void playIndicatorEmitsEventForCurrentDialogNpc() {
        Entity player = new Entity();
        Entity npc = new Entity();

        CommandRegistry registry = new CommandRegistry();
        new DialogCommandModule().register(registry);

        List<FlowAction> actions = registry.dispatch(
            CommandCall.simple("play_indicator", List.of("QUEST_AVAILABLE")),
            new FlowContext(player, npc)
        );

        FlowAction.EmitEvent emitEvent = assertInstanceOf(FlowAction.EmitEvent.class, actions.get(0));
        PlayIndicatorEvent event = assertInstanceOf(PlayIndicatorEvent.class, emitEvent.event());

        assertSame(player, event.player());
        assertSame(npc, event.target());
        assertEquals(OverheadIndicator.OverheadIndicatorType.QUEST_AVAILABLE, event.indicatorType());
        assertNull(event.durationSeconds());
    }

    @Test
    void playIndicatorParsesOptionalDurationSeconds() {
        Entity player = new Entity();
        Entity npc = new Entity();

        CommandRegistry registry = new CommandRegistry();
        new DialogCommandModule().register(registry);

        List<FlowAction> actions = registry.dispatch(
            CommandCall.simple("play_indicator", List.of("INFO", "1.5")),
            new FlowContext(player, npc)
        );

        FlowAction.EmitEvent emitEvent = assertInstanceOf(FlowAction.EmitEvent.class, actions.get(0));
        PlayIndicatorEvent event = assertInstanceOf(PlayIndicatorEvent.class, emitEvent.event());

        assertEquals(1.5f, event.durationSeconds());
    }

    @Test
    void playIndicatorResolvesNpcTargetFromArguments() {
        Entity player = new Entity();
        Entity npc = new Entity();
        Entity otherNpc = new Entity();

        CommandRegistry registry = new CommandRegistry();
        new DialogCommandModule(() -> new EntityLookup() {
            @Override
            public Entity find(String name) {
                return "Npc-1".equals(name) ? otherNpc : null;
            }

            @Override
            public Entity getPlayer() {
                return player;
            }
        }).register(registry);

        List<FlowAction> actions = registry.dispatch(
            CommandCall.simple("play_indicator", List.of("Npc-1", "HAPPY", "2")),
            new FlowContext(player, npc)
        );

        FlowAction.EmitEvent emitEvent = assertInstanceOf(FlowAction.EmitEvent.class, actions.get(0));
        PlayIndicatorEvent event = assertInstanceOf(PlayIndicatorEvent.class, emitEvent.event());

        assertSame(otherNpc, event.target());
        assertEquals(OverheadIndicator.OverheadIndicatorType.HAPPY, event.indicatorType());
        assertEquals(2f, event.durationSeconds());
    }
}
