package com.github.tilcob.game.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.Map;

public class KeyboardController extends InputAdapter {
    private static final Map<Integer, Command> KEY_MAPPING = Map.ofEntries(
        Map.entry(Input.Keys.W, Command.UP),
        Map.entry(Input.Keys.S, Command.DOWN),
        Map.entry(Input.Keys.A, Command.LEFT),
        Map.entry(Input.Keys.D, Command.RIGHT),
        Map.entry(Input.Keys.SPACE, Command.SELECT),
        Map.entry(Input.Keys.ESCAPE, Command.CANCEL)
    );

    private final boolean[] commandState;
    private final Map<Class<? extends ControllerState>, ControllerState> stateCache;
    private ControllerState activeState;

    public KeyboardController(Class<? extends ControllerState> initialState, Engine engine, Stage stage) {
        this.stateCache = new HashMap<>();
        this.activeState = null;
        this.commandState = new boolean[Command.values().length];

        this.stateCache.put(IdleControllerState.class, new IdleControllerState());
        if (engine != null) this.stateCache.put(GameControllerState.class, new GameControllerState(engine));
        if (stage != null) this.stateCache.put(UiControllerState.class, new UiControllerState(stage));

        setActiveState(initialState);
    }

    public void setActiveState(Class<? extends ControllerState> stateClass) {
        ControllerState state = stateCache.get(stateClass);

        if(state == null) throw new GdxRuntimeException("Controller state not found: " + stateClass.getSimpleName());

        for (Command command : Command.values()) {
            if (activeState != null && commandState[command.ordinal()]) {
                activeState.keyUp(command);
            }
            commandState[command.ordinal()] = false;
        }

        this.activeState = state;
    }

    @Override
    public boolean keyDown(int keycode) {
        Command command = KEY_MAPPING.get(keycode);
        if(command == null) return false;

        commandState[command.ordinal()] = true;
        activeState.keyDown(command);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        Command command = KEY_MAPPING.get(keycode);
        if(command == null) return false;
        if (!commandState[command.ordinal()]) return false;

        commandState[command.ordinal()] = false;
        activeState.keyUp(command);
        return true;
    }
}
