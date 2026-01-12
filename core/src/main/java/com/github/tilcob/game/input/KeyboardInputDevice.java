package com.github.tilcob.game.input;

import com.badlogic.gdx.InputAdapter;

public class KeyboardInputDevice extends InputAdapter implements InputDevice {
    private final InputBindings bindings;
    private InputDeviceListener listener;

    public KeyboardInputDevice(InputBindings bindings) {
        this.bindings = bindings;
    }

    @Override
    public void setListener(InputDeviceListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean keyDown(int keycode) {
        Command command = bindings.getCommand(keycode);
        if (command == null) {
            return false;
        }
        if (listener != null) {
            listener.onCommandPressed(command);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        Command command = bindings.getCommand(keycode);
        if (command == null) {
            return false;
        }
        if (listener != null) {
            listener.onCommandReleased(command);
        }
        return true;
    }
}
