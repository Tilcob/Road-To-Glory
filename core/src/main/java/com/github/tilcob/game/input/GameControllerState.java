package com.github.tilcob.game.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tilcob.game.component.Controller;

public class GameControllerState implements ControllerState {
    private final ActiveEntityReference activeEntityReference;

    public GameControllerState(ActiveEntityReference activeEntityReference) {
        this.activeEntityReference = activeEntityReference;
    }

    @Override
    public void keyDown(Command command) {
        Entity entity = activeEntityReference.get();
        if (entity == null) return;
        Controller controller = Controller.MAPPER.get(entity);
        if (controller == null) return;
        controller.getPressedCommands().add(command);
        controller.getCommandBuffer().put(command, TimeUtils.millis() / 1000f);
        controller.getHeldCommands().add(command);
    }

    @Override
    public void keyUp(Command command) {
        Entity entity = activeEntityReference.get();
        if (entity == null) return;
        Controller controller = Controller.MAPPER.get(entity);
        if (controller == null) return;
        controller.getReleasedCommands().add(command);
        controller.getHeldCommands().remove(command);
    }
}
