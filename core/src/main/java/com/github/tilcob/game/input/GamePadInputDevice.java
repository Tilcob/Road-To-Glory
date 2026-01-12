package com.github.tilcob.game.input;

public class GamePadInputDevice implements InputDevice {
    private InputDeviceListener listener;

    @Override
    public void setListener(InputDeviceListener listener) {
        this.listener = listener;
    }

    public void fireCommandPressed(Command command) {
        if (listener != null) {
            listener.onCommandPressed(command);
        }
    }

    public void fireCommandReleased(Command command) {
        if (listener != null) {
            listener.onCommandReleased(command);
        }
    }
}
