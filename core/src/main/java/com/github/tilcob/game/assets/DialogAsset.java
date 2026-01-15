package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public enum DialogAsset {
    SHOPKEEPER("Shopkeeper"),
    GUARD("Guard"),
    NPC1("Npc-1", "Npc-1"),
    NPC2("Npc-2", "Npc-2"),;

    private final String fileName;
    private final String npcName;

    DialogAsset(String npcName) {
        this(npcName, null);
    }

    DialogAsset(String npcName, String fileName) {
        this.npcName = npcName;
        this.fileName = fileName == null ? name().toLowerCase() : fileName;
    }

    public FileHandle getFileHandle() {
        return Gdx.files.internal("dialogs/" + fileName + ".yarn");
    }

    public String getNpcName() {
        return npcName;
    }
}
