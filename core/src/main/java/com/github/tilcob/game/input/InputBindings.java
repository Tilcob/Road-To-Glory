package com.github.tilcob.game.input;

import com.badlogic.gdx.Input;

import java.lang.reflect.Array;
import java.util.*;

public class InputBindings {
    private static final Map<String, Integer> KEY_ALIASES = createKeyAliases();

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
        for (Map.Entry<Command, Set<Integer>> entry : commandBindings.entrySet()) {
            if (entry.getKey() != command) {
                entry.getValue().remove(keycode);
            }
        }
        Set<Integer> keycodes = new LinkedHashSet<>();
        keycodes.add(keycode);
        commandBindings.put(command, keycodes);
        rebuildKeyMapping();
    }

    public void resetToDefaults() {
        commandBindings.clear();
        commandBindings.putAll(defaultCommandBindings());
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
        Integer alias = KEY_ALIASES.get(normalized);
        if (alias != null) {
            return alias;
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

    private static Map<String, Integer> createKeyAliases() {
        Map<String, Integer> aliases = new HashMap<>();
        addAlias(aliases, "SPACE", Input.Keys.SPACE);
        addAlias(aliases, "SPACEBAR", Input.Keys.SPACE);
        addAlias(aliases, "ESC", Input.Keys.ESCAPE);
        addAlias(aliases, "ESCAPE", Input.Keys.ESCAPE);
        addAlias(aliases, "RETURN", Input.Keys.ENTER);
        addAlias(aliases, "ENTER", Input.Keys.ENTER);
        addAlias(aliases, "NUMPAD_ENTER", Input.Keys.NUMPAD_ENTER);
        addAlias(aliases, "KP_ENTER", Input.Keys.NUMPAD_ENTER);
        addAlias(aliases, "TAB", Input.Keys.TAB);
        addAlias(aliases, "BACKSPACE", Input.Keys.BACKSPACE);
        addAlias(aliases, "BKSP", Input.Keys.BACKSPACE);
        addAlias(aliases, "DEL", Input.Keys.DEL);
        addAlias(aliases, "DELETE", Input.Keys.DEL);
        addAlias(aliases, "INS", Input.Keys.INSERT);
        addAlias(aliases, "INSERT", Input.Keys.INSERT);
        addAlias(aliases, "HOME", Input.Keys.HOME);
        addAlias(aliases, "END", Input.Keys.END);
        addAlias(aliases, "PGUP", Input.Keys.PAGE_UP);
        addAlias(aliases, "PAGEUP", Input.Keys.PAGE_UP);
        addAlias(aliases, "PGDN", Input.Keys.PAGE_DOWN);
        addAlias(aliases, "PAGEDOWN", Input.Keys.PAGE_DOWN);
        addAlias(aliases, "UP", Input.Keys.UP);
        addAlias(aliases, "DOWN", Input.Keys.DOWN);
        addAlias(aliases, "LEFT", Input.Keys.LEFT);
        addAlias(aliases, "RIGHT", Input.Keys.RIGHT);
        addAlias(aliases, "ARROW_UP", Input.Keys.UP);
        addAlias(aliases, "ARROW_DOWN", Input.Keys.DOWN);
        addAlias(aliases, "ARROW_LEFT", Input.Keys.LEFT);
        addAlias(aliases, "ARROW_RIGHT", Input.Keys.RIGHT);
        addAlias(aliases, "CTRL", Input.Keys.CONTROL_LEFT);
        addAlias(aliases, "CONTROL", Input.Keys.CONTROL_LEFT);
        addAlias(aliases, "CTRL_LEFT", Input.Keys.CONTROL_LEFT);
        addAlias(aliases, "CTRL_RIGHT", Input.Keys.CONTROL_RIGHT);
        addAlias(aliases, "ALT", Input.Keys.ALT_LEFT);
        addAlias(aliases, "ALT_LEFT", Input.Keys.ALT_LEFT);
        addAlias(aliases, "ALT_RIGHT", Input.Keys.ALT_RIGHT);
        addAlias(aliases, "SHIFT", Input.Keys.SHIFT_LEFT);
        addAlias(aliases, "SHIFT_LEFT", Input.Keys.SHIFT_LEFT);
        addAlias(aliases, "SHIFT_RIGHT", Input.Keys.SHIFT_RIGHT);
        addAlias(aliases, "CMD", Input.Keys.SYM);
        addAlias(aliases, "COMMAND", Input.Keys.SYM);
        addAlias(aliases, "META", Input.Keys.SYM);
        addAlias(aliases, "SUPER", Input.Keys.SYM);
        addAlias(aliases, "CAPSLOCK", Input.Keys.CAPS_LOCK);
        addAlias(aliases, "CAPS_LOCK", Input.Keys.CAPS_LOCK);
        addAlias(aliases, "PRINTSCREEN", Input.Keys.PRINT_SCREEN);
        addAlias(aliases, "PRINT_SCREEN", Input.Keys.PRINT_SCREEN);
        addAlias(aliases, "PRTSC", Input.Keys.PRINT_SCREEN);
        addAlias(aliases, "SCROLLLOCK", Input.Keys.SCROLL_LOCK);
        addAlias(aliases, "SCROLL_LOCK", Input.Keys.SCROLL_LOCK);
        addAlias(aliases, "PAUSE", Input.Keys.PAUSE);
        return Collections.unmodifiableMap(aliases);
    }

    private static void addAlias(Map<String, Integer> aliases, String name, int keycode) {
        if (keycode == Input.Keys.UNKNOWN) {
            return;
        }
        aliases.put(name, keycode);
    }

    public static class BindingFile {
        public HashMap<String, Object> bindings = new HashMap<>();
    }
}
