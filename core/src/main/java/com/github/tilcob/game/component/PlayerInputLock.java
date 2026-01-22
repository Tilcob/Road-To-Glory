package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.input.Command;

import java.util.Arrays;
import java.util.EnumSet;

public class PlayerInputLock implements Component {
    public static final ComponentMapper<PlayerInputLock> MAPPER = ComponentMapper.getFor(PlayerInputLock.class);

    private final EnumSet<Command> allowed = EnumSet.noneOf(Command.class);

    public boolean isAllowed(Command command) {
        return command != null && allowed.contains(command);
    }

    public static PlayerInputLock allow(Command... commands) {
        PlayerInputLock lock = new PlayerInputLock();
        if (commands != null)  lock.allowed.addAll(Arrays.asList(commands));
        return lock;
    }
}
