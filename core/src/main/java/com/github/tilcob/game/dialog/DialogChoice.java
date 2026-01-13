package com.github.tilcob.game.dialog;

import com.badlogic.gdx.utils.Array;

public class DialogChoice {
    private String text;
    private Array<String> lines;

    public DialogChoice(String text, Array<String> lines) {
        this.text = text;
        this.lines = lines;
    }

    public DialogChoice() {
    }

    public String text() {
        return text;
    }

    public Array<String> lines() {
        return lines;
    }
}
