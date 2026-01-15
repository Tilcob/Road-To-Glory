package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public enum QuestAsset {
    welcome_to_town,
    talk_to_jakob,
    talk,;

    public FileHandle getFile() {
        return Gdx.files.internal("quests/" + name().toLowerCase() + ".json");
    }
}
