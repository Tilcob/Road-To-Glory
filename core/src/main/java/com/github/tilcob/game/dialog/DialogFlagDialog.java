package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class DialogFlagDialog {
    private String flag;
    private Array<String> lines;

    public DialogFlagDialog() {
    }

    public DialogFlagDialog(String flag, Array<String> lines) {
        this.flag = flag;
        this.lines = lines;
    }

    public String flag() {
        return flag;
    }

    public Array<String> lines() {
        return lines;
    }
}
