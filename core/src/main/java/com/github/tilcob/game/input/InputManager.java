package com.github.tilcob.game.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

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
    private final List<InputProcessor> modalProcessors;
    private final InputBindings bindings;
    private final InputBindingsStorage bindingsStorage;
    private final boolean[] commandState;
    private ControllerState activeState;
    private InputProcessor listeningProcessor;

    public InputManager(InputMultiplexer inputMultiplexer) {
        this(inputMultiplexer, null, null);
    }

    public InputManager(InputMultiplexer inputMultiplexer,
                        InputBindings bindings,
                        InputBindingsStorage bindingsStorage) {
        this.inputMultiplexer = inputMultiplexer;
        this.stateCache = new HashMap<>();
        this.devices = new ArrayList<>();
        this.externalProcessors = new ArrayList<>();
        this.deviceProcessors = new ArrayList<>();
        this.modalProcessors = new ArrayList<>();
        this.commandState = new boolean[Command.values().length];
        this.bindings = bindings;
        this.bindingsStorage = bindingsStorage;
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
            throw new IllegalArgumentException(
                "Controller state not configured: " + stateClass.getName() +
                    ". Available: " + stateCache.keySet()
            );
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
        for (InputProcessor processor : modalProcessors) {
            inputMultiplexer.addProcessor(processor);
        }
        for (InputProcessor processor : deviceProcessors) {
            inputMultiplexer.addProcessor(processor);
        }
        for (InputProcessor processor : externalProcessors) {
            inputMultiplexer.addProcessor(processor);
        }
    }

    public void listenForNextKey(IntConsumer listener) {
        if (listener == null) {
            return;
        }
        stopListeningForKey();
        listeningProcessor = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.UNKNOWN) {
                    return false;
                }
                listener.accept(keycode);
                stopListeningForKey();
                return true;
            }
        };
        modalProcessors.add(listeningProcessor);
        rebuildProcessors();
    }

    public void stopListeningForKey() {
        if (listeningProcessor == null) {
            return;
        }
        modalProcessors.remove(listeningProcessor);
        listeningProcessor = null;
        rebuildProcessors();
    }

    public InputBindings getBindings() {
        return bindings;
    }

    public void saveBindings() {
        if (bindingsStorage != null && bindings != null) {
            bindingsStorage.save(bindings);
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
