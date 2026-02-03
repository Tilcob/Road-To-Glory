package com.github.tilcob.game.input;

import com.badlogic.gdx.Input;

import java.lang.reflect.Array;
import java.util.*;

public class InputBindings {
    private final Map<Command, Set<Integer>> commandBindings;
    private final Map<Integer, Command> keyMapping;

    public InputBindings(Map<Command, Set<Integer>> commandBindings) {
        this.commandBindings = new EnumMap<>(Command.class);
        this.commandBindings.putAll(commandBindings);
        this.keyMapping = new HashMap<>();
        rebuildKeyMapping();
    }

    public static InputBindings defaultBindings() {
        return new InputBindings(defaultCommandBindings());
    }

    public static InputBindings fromBindingFile(BindingFile bindingFile) {
        Map<Command, Set<Integer>> bindings = defaultCommandBindings();
        if (bindingFile == null || bindingFile.bindings == null) {
            return new InputBindings(bindings);
        }
        for (Map.Entry<String, Object> entry : bindingFile.bindings.entrySet()) {
            Command command = parseCommand(entry.getKey());
            if (command == null) continue;
            Set<Integer> keycodes = parseKeycodes(entry.getValue());
            if (!keycodes.isEmpty()) {
                bindings.put(command, keycodes);
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
        Set<Integer> keycodes = new LinkedHashSet<>();
        keycodes.add(keycode);
        commandBindings.put(command, keycodes);
        rebuildKeyMapping();
    }

    public Map<Command, Set<Integer>> getCommandBindings() {
        Map<Command, Set<Integer>> copy = new EnumMap<>(Command.class);
        for (Map.Entry<Command, Set<Integer>> entry : commandBindings.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public BindingFile toBindingFile() {
        BindingFile file = new BindingFile();
        for (Map.Entry<Command, Set<Integer>> entry : commandBindings.entrySet()) {
            List<String> keys = new ArrayList<>();
            for (int keycode : entry.getValue()) {
                keys.add(Input.Keys.toString(keycode));
            }
            if (keys.size() == 1) {
                file.bindings.put(entry.getKey().name(), keys.get(0));
            } else {
                file.bindings.put(entry.getKey().name(), keys);
            }
        }
        return file;
    }

    private void rebuildKeyMapping() {
        keyMapping.clear();
        for (Map.Entry<Command, Set<Integer>> entry : commandBindings.entrySet()) {
            for (int keycode : entry.getValue()) {
                keyMapping.put(keycode, entry.getKey());
            }
        }
    }

    private static Map<Command, Set<Integer>> defaultCommandBindings() {
        Map<Command, Set<Integer>> bindings = new EnumMap<>(Command.class);
        bindings.put(Command.UP, newKeySet(Input.Keys.W));
        bindings.put(Command.DOWN, newKeySet(Input.Keys.S));
        bindings.put(Command.LEFT, newKeySet(Input.Keys.A));
        bindings.put(Command.RIGHT, newKeySet(Input.Keys.D));
        bindings.put(Command.SELECT, newKeySet(Input.Keys.SPACE));
        bindings.put(Command.PAUSE, newKeySet(Input.Keys.ESCAPE));
        bindings.put(Command.INTERACT, newKeySet(Input.Keys.E));
        bindings.put(Command.INVENTORY, newKeySet(Input.Keys.I));
        bindings.put(Command.SKILLS, newKeySet(Input.Keys.K));
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
        String normalized = keyName.trim().toUpperCase();
        if ("SPACE".equals(normalized) || "SPACEBAR".equals(normalized)) {
            return Input.Keys.SPACE;
        }
        if ("ESC".equals(normalized) || "ESCAPE".equals(normalized)) {
            return Input.Keys.ESCAPE;
        }
        try {
            return Input.Keys.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return Input.Keys.UNKNOWN;
        }
    }

    private static Set<Integer> parseKeycodes(Object value) {
        Set<Integer> keycodes = new LinkedHashSet<>();
        if (value == null) {
            return keycodes;
        }
        if (value instanceof String) {
            addKeycode(keycodes, (String) value);
            return keycodes;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (item != null) {
                    addKeycode(keycodes, item.toString());
                }
            }
            return keycodes;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(value, i);
                if (item != null) {
                    addKeycode(keycodes, item.toString());
                }
            }
            return keycodes;
        }
        addKeycode(keycodes, value.toString());
        return keycodes;
    }

    private static void addKeycode(Set<Integer> keycodes, String keyName) {
        int keycode = parseKeycode(keyName);
        if (keycode != Input.Keys.UNKNOWN) {
            keycodes.add(keycode);
        }
    }

    private static Set<Integer> newKeySet(int keycode) {
        Set<Integer> keycodes = new LinkedHashSet<>();
        keycodes.add(keycode);
        return keycodes;
    }

    public static class BindingFile {
        public HashMap<String, Object> bindings = new HashMap<>();
    }
}
