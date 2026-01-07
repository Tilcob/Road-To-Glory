package com.github.tilcob.game.save.migration;

import com.github.tilcob.game.save.states.GameState;

public interface Migration {
    int fromVersion();
    void migrate(GameState state);
}
