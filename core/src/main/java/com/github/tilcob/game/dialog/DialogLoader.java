package com.github.tilcob.game.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.assets.DialogAsset;

import java.util.HashMap;
import java.util.Map;

public class DialogLoader {
    private final Json json;
    private final YarnDialogLoader yarnDialogLoader;

    public DialogLoader() {
        this.json = new Json();
        this.yarnDialogLoader = new YarnDialogLoader();
        json.setIgnoreDeprecated(true);
    }

    public Map<String, DialogData> load(DialogAsset asset) {
        FileHandle file = asset.getFileHandle();

        if ("yarn".equalsIgnoreCase(file.extension())) {
            Map<String, DialogData> dialogs = new HashMap<>();
            dialogs.put(file.nameWithoutExtension(), yarnDialogLoader.load(file));
            return dialogs;
        }

        MapDialogData mapDialogData = json.fromJson(MapDialogData.class, file);
        Map<String, DialogData> dialogs = new HashMap<>();
        if (mapDialogData == null || mapDialogData.getNpcs() == null) {
            return dialogs;
        }
        for (ObjectMap.Entry<String, DialogData> entry : mapDialogData.getNpcs()) {
            dialogs.put(entry.key, entry.value);
        }
        return dialogs;
    }
}
