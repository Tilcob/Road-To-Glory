package com.github.tilcob.game.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.github.tilcob.game.input.Command;

public class UiEvent extends Event {
    private final Command command;

    public UiEvent(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
