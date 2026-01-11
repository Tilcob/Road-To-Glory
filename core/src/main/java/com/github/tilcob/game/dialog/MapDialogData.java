package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.ObjectMap;

import java.util.HashMap;
import java.util.Map;

public class MapDialogData {
    private final ObjectMap<String, DialogData> npcs = new ObjectMap<>();

    public ObjectMap<String, DialogData> getNpcs() {
        return npcs;
    }
}
