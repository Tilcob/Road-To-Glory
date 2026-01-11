package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class DialogData {
    private Array<String> idle;
    private QuestDialog questDialog;

    public DialogData(Array<String> idle, QuestDialog questDialog) {
        this.idle = idle;
        this.questDialog = questDialog;
    }

    public DialogData() {
    }

    public Array<String> idle() {
        return idle;
    }

    public QuestDialog questDialog() {
        return questDialog;
    }
}
