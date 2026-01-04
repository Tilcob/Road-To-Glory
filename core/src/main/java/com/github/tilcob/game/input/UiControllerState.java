package com.github.tilcob.game.input;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UiEvent;

public class UiControllerState implements ControllerState {
    private final GameEventBus eventBus;

    public UiControllerState(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void keyDown(Command command) {
        eventBus.fire(new UiEvent(command));
    }
}
