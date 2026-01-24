package com.github.tilcob.game.event;

import com.github.tilcob.game.input.Command;

public class UiEvent implements GameEvent {
    private final Command command;
    private final Action action;
    private boolean handled;

    public UiEvent(Command command, Action action) {
        this.command = command;
        this.action = action;
    }

    public Command command() {
        return command;
    }

    public Action action() {
        return action;
    }

    @Override
    public boolean isHandled() {
        return handled;
    }

    @Override
    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public enum Action {
        PRESS,
        RELEASE,
        REPEAT
    }
}
