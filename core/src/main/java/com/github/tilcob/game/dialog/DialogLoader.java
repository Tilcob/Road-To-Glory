package com.github.tilcob.game.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.github.tilcob.game.assets.DialogAsset;

public class DialogLoader {
    private final Json json;

    public DialogLoader() {
        this.json = new Json();
        json.setIgnoreDeprecated(true);
    }

    public MapDialogData load(DialogAsset asset) {
        FileHandle file = asset.getFileHandle();
        return json.fromJson(MapDialogData.class, file);
    }
}
