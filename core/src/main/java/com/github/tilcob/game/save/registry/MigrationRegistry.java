package com.github.tilcob.game.save.registry;

import com.github.tilcob.game.save.migration.Migration;
import com.github.tilcob.game.save.migration.MigrationV2;
import com.github.tilcob.game.save.migration.MigrationV3;
import com.github.tilcob.game.save.migration.MigrationV4;
import com.github.tilcob.game.save.states.GameState;

import java.util.ArrayList;
import java.util.List;

public class MigrationRegistry {
    private final List<Migration> migrations = new ArrayList<>();

    public MigrationRegistry() {
        migrations.add(new MigrationV2());
        migrations.add(new MigrationV3());
        migrations.add(new MigrationV4());
    }

    public void migrate(GameState state) {
        int version = state.getSaveVersion();

        for (Migration migration : migrations) {
            if (version < migration.fromVersion()) {
                migration.migrate(state);
                state.setSaveVersion(migration.fromVersion());
            }
        }
    }
}
