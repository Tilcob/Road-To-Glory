package com.github.tilcob.game.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.assets.DialogAsset;

import java.util.HashMap;
import java.util.Map;

public class DialogLoader {
    private final YarnDialogLoader yarnDialogLoader;

    public DialogLoader() {
        this.yarnDialogLoader = new YarnDialogLoader();
    }

    public Map<String, DialogData> load(DialogAsset asset) {
        FileHandle file = asset.getFileHandle();
        Map<String, DialogData> dialogs = new HashMap<>();
        if (!"yarn".equalsIgnoreCase(file.extension())) {
            return dialogs;
        }
        dialogs.put(file.nameWithoutExtension(), yarnDialogLoader.load(file));
        return dialogs;
    }
}
