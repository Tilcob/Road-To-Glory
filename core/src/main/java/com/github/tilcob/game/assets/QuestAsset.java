package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public enum QuestAsset {
    MAIN("main.json"),;

    private final String fileName;

    QuestAsset(String fileName) {
        this.fileName = fileName;
    }

    public FileHandle getFile() {
        return Gdx.files.internal("quests/" + fileName);
    }
}
