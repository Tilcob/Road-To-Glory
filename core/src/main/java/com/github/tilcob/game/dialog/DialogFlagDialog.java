package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.yarn.script.ScriptEvent;

public class DialogFlagDialog {
    private String flag;
    private Array<ScriptEvent> lines;

    public DialogFlagDialog() {
    }

    public DialogFlagDialog(String flag, Array<ScriptEvent> lines) {
        this.flag = flag;
        this.lines = lines;
    }

    public String flag() {
        return flag;
    }

    public Array<ScriptEvent> lines() {
        return lines;
    }
}
