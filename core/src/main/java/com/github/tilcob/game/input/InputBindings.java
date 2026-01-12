package com.github.tilcob.game.input;

import com.badlogic.gdx.Input;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class InputBindings {
    private final Map<Command, Integer> commandBindings;
    private final Map<Integer, Command> keyMapping;

    public InputBindings(Map<Command, Integer> commandBindings) {
        this.commandBindings = new EnumMap<>(Command.class);
        this.commandBindings.putAll(commandBindings);
        this.keyMapping = new HashMap<>();
        rebuildKeyMapping();
    }

    public static InputBindings defaultBindings() {
        return new InputBindings(defaultCommandBindings());
    }

    public static InputBindings fromBindingFile(BindingFile bindingFile) {
        Map<Command, Integer> bindings = defaultCommandBindings();
        if (bindingFile == null || bindingFile.bindings == null) {
            return new InputBindings(bindings);
        }
        for (Map.Entry<String, String> entry : bindingFile.bindings.entrySet()) {
            Command command = parseCommand(entry.getKey());
            int keycode = parseKeycode(entry.getValue());
            if (command != null && keycode != Input.Keys.UNKNOWN) {
                bindings.put(command, keycode);
            }
        }
        return new InputBindings(bindings);
    }

    public Command getCommand(int key) {
        return keyMapping.get(key);
    }

    public void setBinding(Command command, int keycode) {
        if (command == null || keycode == Input.Keys.UNKNOWN) {
            return;
        }
        commandBindings.put(command, keycode);
        rebuildKeyMapping();
    }

    public Map<Command, Integer> getCommandBindings() {
        return Collections.unmodifiableMap(commandBindings);
    }

    public BindingFile toBindingFile() {
        BindingFile file = new BindingFile();
        for (Map.Entry<Command, Integer> entry : commandBindings.entrySet()) {
            file.bindings.put(entry.getKey().name(), Input.Keys.toString(entry.getValue()));
        }
        return file;
    }

    private void rebuildKeyMapping() {
        keyMapping.clear();
        for (Map.Entry<Command, Integer> entry : commandBindings.entrySet()) {
            keyMapping.put(entry.getValue(), entry.getKey());
        }
    }

    private static Map<Command, Integer> defaultCommandBindings() {
        Map<Command, Integer> bindings = new EnumMap<>(Command.class);
        bindings.put(Command.UP, Input.Keys.W);
        bindings.put(Command.DOWN, Input.Keys.S);
        bindings.put(Command.LEFT, Input.Keys.A);
        bindings.put(Command.RIGHT, Input.Keys.D);
        bindings.put(Command.SELECT, Input.Keys.SPACE);
        bindings.put(Command.CANCEL, Input.Keys.ESCAPE);
        bindings.put(Command.INTERACT, Input.Keys.E);
        bindings.put(Command.INVENTORY, Input.Keys.I);
        return bindings;
    }

    private static Command parseCommand(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return Command.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static int parseKeycode(String keyName) {
        if (keyName == null || keyName.isBlank()) {
            return Input.Keys.UNKNOWN;
        }
        return Input.Keys.valueOf(keyName.trim().toUpperCase());
    }

    public static class BindingFile {
        public Map<String, String> bindings = new HashMap<>();
    }
}
