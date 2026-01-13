package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class DialogData {
    private Array<String> idle;
    private Array<DialogChoice> choices;
    private QuestDialog questDialog;

    public DialogData(Array<String> idle, Array<DialogChoice> choices, QuestDialog questDialog) {
        this.idle = idle;
        this.choices = choices;
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

    public QuestDialog questDialog() {
        return questDialog;
    }
}
