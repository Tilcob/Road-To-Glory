package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.ObjectMap;

public class MapDialogData {
    private final ObjectMap<String, DialogData> npcs = new ObjectMap<>();

    public ObjectMap<String, DialogData> getNpcs() {
        return npcs;
    }
}
