package com.github.tilcob.game.save.migration;

import com.github.tilcob.game.save.states.GameState;

public class MigrationV2 implements Migration {

    @Override
    public int fromVersion() {
        return 2;
    }

    @Override
    public void migrate(GameState state) {
        if (state.getCurrentMap() != null) {
            state.setCurrentMapByName(state.getCurrentMap().name());
        }
    }
}
