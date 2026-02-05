package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.event.PlayIndicatorEvent;
import com.github.tilcob.game.flow.CommandCall;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowAction;
import com.github.tilcob.game.flow.FlowContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

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
    }
}
