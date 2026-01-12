package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.input.Command;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class Controller implements Component {
    public static final ComponentMapper<Controller> MAPPER = ComponentMapper.getFor(Controller.class);

    private final boolean playerControlled;
    private final EnumSet<Command> pressedCommand;
    private final EnumSet<Command> releasedCommand;
    private final EnumSet<Command> heldCommand;
    private final Map<Command, Float> commandBuffer;

    public Controller() {
        this(false);
    }

    public Controller(boolean playerControlled) {
        this.playerControlled = playerControlled;
        this.pressedCommand = EnumSet.noneOf(Command.class);
        this.releasedCommand = EnumSet.noneOf(Command.class);
        this.heldCommand = EnumSet.noneOf(Command.class);
        this.commandBuffer = new EnumMap<>(Command.class);
    }

    public boolean isPlayerControlled() {
        return playerControlled;
    }

    public EnumSet<Command> getPressedCommands() {
        return pressedCommand;
    }

    public EnumSet<Command> getReleasedCommands() {
        return releasedCommand;
    }

    public EnumSet<Command> getHeldCommands() {
        return heldCommand;
    }

    public Map<Command, Float> getCommandBuffer() {
        return commandBuffer;
    }
}
