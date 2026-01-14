package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class DialogData {
    private Array<String> idle;
    private Array<DialogChoice> choices;
    private Array<DialogFlagDialog> flagDialogs;
    private QuestDialog questDialog;

    public DialogData(Array<String> idle, Array<DialogChoice> choices, Array<DialogFlagDialog> flagDialogs, QuestDialog questDialog) {
        this.idle = idle;
        this.choices = choices;
        this.flagDialogs = flagDialogs;
        this.questDialog = questDialog;
    }

    public DialogData() {
    }

    public Array<String> idle() {
        return idle;
    }

    public Array<DialogChoice> choices() {
        return choices;
    }

    public Array<DialogFlagDialog> flagDialogs() {
        return flagDialogs;
    }

    public QuestDialog questDialog() {
        return questDialog;
    }
}
