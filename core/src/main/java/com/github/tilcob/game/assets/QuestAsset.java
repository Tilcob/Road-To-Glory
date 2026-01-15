package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public enum QuestAsset {
    Welcome_To_Town,
    Talk_To_Jakob,
    Talk,;

    public FileHandle getFile() {
        return Gdx.files.internal("quests/" + name() + ".json");
    }
}
