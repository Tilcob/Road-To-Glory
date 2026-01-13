package com.github.tilcob.game.input;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Controller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerStateTest {
    @Test
    void sendsCommandsOnlyToActiveEntity() {
        ActiveEntityReference activeEntityReference = new ActiveEntityReference();
        GameControllerState state = new GameControllerState(activeEntityReference);
        Entity player = new Entity();
        Entity npc = new Entity();
        player.add(new Controller(true));
        npc.add(new Controller());
        activeEntityReference.set(player);

        state.keyDown(Command.UP);

        assertTrue(Controller.MAPPER.get(player).getPressedCommands().contains(Command.UP));
        assertFalse(Controller.MAPPER.get(npc).getPressedCommands().contains(Command.UP));
    }

    @Test
    void updatesHeldAndReleasedCommands() {
        ActiveEntityReference activeEntityReference = new ActiveEntityReference();
        GameControllerState state = new GameControllerState(activeEntityReference);
        Entity player = new Entity();
        player.add(new Controller(true));
        activeEntityReference.set(player);

        state.keyDown(Command.DOWN);
        assertTrue(Controller.MAPPER.get(player).getHeldCommands().contains(Command.DOWN));
        assertEquals(1, Controller.MAPPER.get(player).getCommandBuffer().size());

        state.keyUp(Command.DOWN);
        assertTrue(Controller.MAPPER.get(player).getReleasedCommands().contains(Command.DOWN));
        assertFalse(Controller.MAPPER.get(player).getHeldCommands().contains(Command.DOWN));
    }
}
