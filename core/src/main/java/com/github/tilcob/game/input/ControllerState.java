package com.github.tilcob.game.input;

public interface ControllerState {
    void keyDown(Command command);

    default void keyUp(Command command)  {
    }
}
