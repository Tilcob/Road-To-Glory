package com.github.tilcob.game.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.tilcob.game.component.Controller;

public class GameControllerState implements ControllerState{
    private final ImmutableArray<Entity> controllerEntities;

    public GameControllerState(ImmutableArray<Entity> controllerEntities) {
        this.controllerEntities = controllerEntities;
    }

    @Override
    public void keyDown(Command command) {
        for (Entity entity : controllerEntities) {
            Controller controller = Controller.MAPPER.get(entity);
            controller.getPressedCommands().add(command);
            controller.getHeldCommands().add(command);
        }
    }

    @Override
    public void keyUp(Command command) {
        for (Entity entity : controllerEntities) {
            Controller controller = Controller.MAPPER.get(entity);
            controller.getReleasedCommands().add(command);
            controller.getHeldCommands().remove(command);
        }
    }
}
