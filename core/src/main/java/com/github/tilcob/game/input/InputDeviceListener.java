package com.github.tilcob.game.input;

public interface InputDeviceListener {
    void onCommandPressed(Command command);
    void onCommandReleased(Command command);
}
