package com.github.tilcob.game.input;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central input routing for the game.
 * <p>
 * Only active screens (e.g. {@code MenuScreen}, {@code GameScreen}) should call
 * {@link #setInputProcessors(InputProcessor...)} from their lifecycle methods like {@code show()}.
 * Controller states should only update their internal state and must not add or remove input
 * processors.
 */
public class InputManager implements InputDeviceListener {
    private final InputMultiplexer inputMultiplexer;
    private final Map<Class<? extends ControllerState>, ControllerState> stateCache;
    private final List<InputDevice> devices;
    private final List<InputProcessor> externalProcessors;
    private final List<InputProcessor> deviceProcessors;
    private final boolean[] commandState;
    private ControllerState activeState;

    public InputManager(InputMultiplexer inputMultiplexer) {
        this.inputMultiplexer = inputMultiplexer;
        this.stateCache = new HashMap<>();
        this.devices = new ArrayList<>();
        this.externalProcessors = new ArrayList<>();
        this.deviceProcessors = new ArrayList<>();
        this.commandState = new boolean[Command.values().length];
    }

    public void addDevice(InputDevice device) {
        if (device == null) {
            return;
        }
        devices.add(device);
        device.setListener(this);
        if (device instanceof InputProcessor inputProcessor) {
            deviceProcessors.add(inputProcessor);
            rebuildProcessors();
        }
    }

    public void configureStates(Class<? extends ControllerState> initialState, ControllerState... states) {
        stateCache.clear();
        if (states != null) {
            for (ControllerState state : states) {
                if (state != null) {
                    stateCache.put(state.getClass(), state);
                }
            }
        }
        setActiveState(initialState);
    }

    public void setActiveState(Class<? extends ControllerState> stateClass) {
        if (stateClass == null) {
            activeState = null;
            return;
        }
        ControllerState state = stateCache.get(stateClass);
        if (state == null) {
            throw new GdxRuntimeException("Controller state not found: " + stateClass.getSimpleName());
        }

        for (Command command : Command.values()) {
            if (activeState != null && commandState[command.ordinal()]) {
                activeState.keyUp(command);
            }
            commandState[command.ordinal()] = false;
        }

        this.activeState = state;
    }

    public void setInputProcessors(InputProcessor... processors) {
        externalProcessors.clear();
        if (processors != null) {
            for (InputProcessor processor : processors) {
                if (processor != null) {
                    externalProcessors.add(processor);
                }
            }
        }
        rebuildProcessors();
    }

    private void rebuildProcessors() {
        inputMultiplexer.clear();
        for (InputProcessor processor : deviceProcessors) {
            inputMultiplexer.addProcessor(processor);
        }
        for (InputProcessor processor : externalProcessors) {
            inputMultiplexer.addProcessor(processor);
        }
    }


    @Override
    public void onCommandPressed(Command command) {
        if (command == null || activeState == null) {
            return;
        }
        if (commandState[command.ordinal()]) {
            return;
        }
        commandState[command.ordinal()] = true;
        activeState.keyDown(command);
    }

    @Override
    public void onCommandReleased(Command command) {
        if (command == null || activeState == null) {
            return;
        }
        if (!commandState[command.ordinal()]) {
            return;
        }
        commandState[command.ordinal()] = false;
        activeState.keyUp(command);
    }
}
