package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public enum DialogAsset {
    MAIN("main.json"),;

    private final String fileName;

    DialogAsset(String fileName) {
        this.fileName = fileName;
    }

    public FileHandle getFileHandle() {
        return Gdx.files.internal("dialogs/" + fileName);
    }
}
