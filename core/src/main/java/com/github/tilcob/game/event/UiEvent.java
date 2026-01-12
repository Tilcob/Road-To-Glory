package com.github.tilcob.game.event;

import com.github.tilcob.game.input.Command;

public record UiEvent(Command command, Action action) {

    public enum Action {
        PRESS,
        RELEASE,
        REPEAT
    }
}
