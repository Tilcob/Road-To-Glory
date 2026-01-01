package com.github.tilcob.game.input;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.tilcob.game.event.UiEvent;

public class UiControllerState implements ControllerState {
    private final Stage stage;

    public UiControllerState(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void keyDown(Command command) {
        stage.getRoot().fire(new UiEvent(command));
    }
}
