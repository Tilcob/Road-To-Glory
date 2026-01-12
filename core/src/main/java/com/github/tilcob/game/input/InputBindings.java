package com.github.tilcob.game.input;

import com.badlogic.gdx.Input;

import java.util.HashMap;
import java.util.Map;

public class InputBindings {
    private final Map<Integer, Command> keyMapping;

    public InputBindings(Map<Integer, Command> keyMapping) {
        this.keyMapping = new HashMap<>(keyMapping);
    }

    public static InputBindings defaultBindings() {
        return new InputBindings(Map.ofEntries(
            Map.entry(Input.Keys.W, Command.UP),
            Map.entry(Input.Keys.S, Command.DOWN),
            Map.entry(Input.Keys.A, Command.LEFT),
            Map.entry(Input.Keys.D, Command.RIGHT),
            Map.entry(Input.Keys.SPACE, Command.SELECT),
            Map.entry(Input.Keys.ESCAPE, Command.CANCEL),
            Map.entry(Input.Keys.E, Command.INTERACT),
            Map.entry(Input.Keys.I, Command.INVENTORY)
        ));
    }

    public Command getCommand(int key) {
        return keyMapping.get(key);
    }
}
