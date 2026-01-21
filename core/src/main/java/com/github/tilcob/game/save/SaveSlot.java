package com.github.tilcob.game.save;

import java.util.List;

public enum SaveSlot {
    SLOT_1("slot_1.json"),
    SLOT_2("slot_2.json"),
    SLOT_3("slot_3.json"),
    AUTO("autosave.json"),;

    private final String fileName;

    SaveSlot(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public static List<SaveSlot> standardSlots() {
        return List.of(SLOT_1, SLOT_2, SLOT_3, AUTO);
    }
}
