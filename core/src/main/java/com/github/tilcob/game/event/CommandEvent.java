package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.input.Command;

public class CommandEvent implements GameEvent {
    private final Entity player;
    private final Command command;
    private boolean handled;

    public CommandEvent(Entity player, Command command) {
        this.player = player;
        this.command = command;
    }

    public Entity getPlayer() {
        return player;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public boolean isHandled() {
        return handled;
    }

    @Override
    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
