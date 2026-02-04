package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.Input;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.UiOverlayEvent;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.input.InputBindings;
import com.github.tilcob.game.input.InputManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsViewModel extends ViewModel {
    private final InputManager inputManager;
    private final InputBindings bindings;
    private Command listeningCommand;
    private Integer pendingConflictKey;
    private String conflictMessage;
    private boolean open = false;

    public SettingsViewModel(GameServices services, InputManager inputManager) {
        super(services);
        this.inputManager = inputManager;
        this.bindings = inputManager != null && inputManager.getBindings() != null
            ? inputManager.getBindings()
            : InputBindings.defaultBindings();

        getEventBus().subscribe(UiOverlayEvent.class, this::onOverlayEvent);
    }

    private void onOverlayEvent(UiOverlayEvent event) {
        if (event == null) return;

        switch (event.type()) {
            case OPEN_SETTINGS -> setOpen(true);
            case CLOSE_SETTINGS -> setOpen(false);
            case TOGGLE_SETTINGS -> setOpen(!open);
        }
    }

    private void setOpen(boolean value) {
        if (!value && inputManager != null) {
            inputManager.stopListeningForKey();
            listeningCommand = null;
            pendingConflictKey = null;
            conflictMessage = null;
            propertyChangeSupport.firePropertyChange(Constants.KEYBIND_LISTENING, null, null);
            propertyChangeSupport.firePropertyChange(Constants.KEYBIND_CONFLICT, null, null);
        }
        this.open = setOpen(value, this.open, Constants.OPEN_SETTINGS);
    }

    public float getMusicVolume() {
        return getUiServices().getMusicVolume();
    }

    public float getSoundVolume() {
        return getUiServices().getSoundVolume();
    }

    public void setMusicVolume(float volume) {
        getUiServices().setMusicVolume(volume);
    }

    public void setSoundVolume(float volume) {
        getUiServices().setSoundVolume(volume);
    }

    public void close() {
        gameEventBus.fire(new UiOverlayEvent(UiOverlayEvent.Type.CLOSE_SETTINGS));
    }

    public List<Command> getBindableCommands() {
        Map<Command, java.util.Set<Integer>> commandBindings = bindings.getCommandBindings();
        List<Command> preferredOrder = List.of(
            Command.UP,
            Command.DOWN,
            Command.LEFT,
            Command.RIGHT,
            Command.SELECT,
            Command.PAUSE,
            Command.INTERACT,
            Command.INVENTORY,
            Command.SKILLS
        );
        List<Command> commands = new ArrayList<>();
        for (Command command : preferredOrder) {
            if (commandBindings.containsKey(command)) {
                commands.add(command);
            }
        }
        return commands;
    }

    public String getBindingLabel(Command command) {
        if (command == null) return "";

        Map<Command, java.util.Set<Integer>> commandBindings = bindings.getCommandBindings();
        java.util.Set<Integer> keys = commandBindings.get(command);
        if (keys == null || keys.isEmpty()) {
            return "Unbound";
        }
        StringBuilder builder = new StringBuilder();
        for (int keycode : keys) {
            if (!builder.isEmpty()) {
                builder.append(" / ");
            }
            builder.append(Input.Keys.toString(keycode));
        }
        return builder.toString();
    }

    public Command getListeningCommand() {
        return listeningCommand;
    }

    public String getConflictMessage() {
        return conflictMessage;
    }

    public void startListening(Command command) {
        if (command == null || inputManager == null) return;

        listeningCommand = command;
        pendingConflictKey = null;
        conflictMessage = null;
        propertyChangeSupport.firePropertyChange(Constants.KEYBIND_LISTENING, null, command);
        propertyChangeSupport.firePropertyChange(Constants.KEYBIND_CONFLICT, null, null);
        inputManager.listenForNextKey(this::handleKeyCapture);
    }

    public void resetToDefaults() {
        bindings.resetToDefaults();
        if (inputManager != null) inputManager.saveBindings();

        propertyChangeSupport.firePropertyChange(Constants.KEYBIND_SAVED, null, null);
    }

    private void handleKeyCapture(int keycode) {
        propertyChangeSupport.firePropertyChange(Constants.KEYBIND_KEY_CAPTURED, null, keycode);
        if (listeningCommand == null) return;

        if (pendingConflictKey != null) {
            if (pendingConflictKey == keycode) {
                applyBinding(keycode);
            } else {
                pendingConflictKey = null;
                conflictMessage = null;
                propertyChangeSupport.firePropertyChange(Constants.KEYBIND_CONFLICT, null, null);
                inputManager.listenForNextKey(this::handleKeyCapture);
            }
            return;
        }

        Command existing = bindings.getCommand(keycode);
        if (existing != null && existing != listeningCommand) {
            pendingConflictKey = keycode;
            conflictMessage = Input.Keys.toString(keycode)
                + " is already bound to " + formatCommandName(existing)
                + ". Press again to overwrite.";
            propertyChangeSupport.firePropertyChange(Constants.KEYBIND_CONFLICT, null, conflictMessage);
            inputManager.listenForNextKey(this::handleKeyCapture);
            return;
        }

        applyBinding(keycode);
    }

    private void applyBinding(int keycode) {
        bindings.setBinding(listeningCommand, keycode);
        if (inputManager != null) {
            inputManager.saveBindings();
        }
        listeningCommand = null;
        pendingConflictKey = null;
        conflictMessage = null;
        propertyChangeSupport.firePropertyChange(Constants.KEYBIND_CONFLICT, null, null);
        propertyChangeSupport.firePropertyChange(Constants.KEYBIND_LISTENING, null, null);
        propertyChangeSupport.firePropertyChange(Constants.KEYBIND_SAVED, null, keycode);
    }

    private String formatCommandName(Command command) {
        String name = command.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public boolean isOpen() {
        return open;
    }
}
